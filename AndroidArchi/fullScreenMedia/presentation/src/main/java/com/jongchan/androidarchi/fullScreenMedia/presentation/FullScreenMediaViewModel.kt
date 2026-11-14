package com.jongchan.androidarchi.fullScreenMedia.presentation

import androidx.lifecycle.viewModelScope
import com.jongchan.androidarchi.common.domain.favorite.GetFavoriteItemsUseCase
import com.jongchan.androidarchi.common.domain.favorite.RegisterFavoriteItemUseCase
import com.jongchan.androidarchi.common.domain.favorite.RemoveFavoriteItemUseCase
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.domain.helper.StringResource
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.presentation.mvi.MviViewModel
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaOrigin
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaPage
import com.jongchan.androidarchi.fullScreenMedia.domain.GetFullScreenMediaItemsUseCase
import com.jongchan.androidarchi.fullScreenMedia.domain.tti.FullScreenMediaTTIPage
import com.jongchan.androidarchi.tti.TTIHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FullScreenMediaViewModel.Factory::class)
class FullScreenMediaViewModel @AssistedInject constructor(
    @Assisted private val args: FullScreenMediaPage.Args,
    private val getFullScreenMediaItemsUseCase: GetFullScreenMediaItemsUseCase,
    private val getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
    private val registerFavoriteItemUseCase: RegisterFavoriteItemUseCase,
    private val removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
    private val messageHelper: MessageHelper,
    private val navigationHelper: NavigationHelper,
    // 화면 인스턴스와 수명이 같은 TTIHelper(@ViewModelScoped). View 는 viewModel.ttiHelper 로 마크를 찍고,
    // 이 ViewModel 에 주입된 UseCase 들도 같은 인스턴스를 본다.
    val ttiHelper: TTIHelper,
    private val resourceHelper: ResourceHelper,
) : MviViewModel<FullScreenMediaIntent, FullScreenMediaUIState, FullScreenMediaReducerEvent>(
    FullScreenMediaUIState.empty
) {

    init {
        ttiHelper.startTTITracking(FullScreenMediaTTIPage)
        bootstrap()
    }

    override fun onIntent(intent: FullScreenMediaIntent) {
        when (intent) {
            is FullScreenMediaIntent.AddFavorite -> addFavorite(intent.item)
            is FullScreenMediaIntent.DeleteFavorite -> deleteFavorite(intent.url)
            is FullScreenMediaIntent.ClickBackButton -> clickBackNavigation()
            is FullScreenMediaIntent.SelectPage ->
                dispatch(FullScreenMediaReducerEvent.PageSelected(intent.index))
        }
    }

    override fun reduce(
        state: FullScreenMediaUIState,
        event: FullScreenMediaReducerEvent,
    ): FullScreenMediaUIState = when (event) {
        is FullScreenMediaReducerEvent.Initialized ->
            FullScreenMediaUIState.ready(event.mediaItems, event.initialIndex, event.swipeEnabled)

        FullScreenMediaReducerEvent.EmptyMediaResolved ->
            state.copy(isLoading = false)

        is FullScreenMediaReducerEvent.FavoritesChanged ->
            state.copy(favoriteUrls = event.urls.asImmutableUrls())

        is FullScreenMediaReducerEvent.PageSelected ->
            state.copy(currentIndex = event.index)
    }

    private fun bootstrap() {
        viewModelScope.launch {
            // API 구간(API_REQUEST_READY_TIME / API_RESPONSE_TIME)은 UseCase 안에서 찍힌다.
            val mediaItems = getFullScreenMediaItemsUseCase(args)
            if (mediaItems.isEmpty()) {
                resolveEmpty()
                return@launch
            }
            // 즐겨찾기 진입은 목록에서 시작 위치를 찾고 좌우 스와이프 페이징, 그 외 진입은 단일 항목만 노출한다.
            val isFromFavorite = args.origin == FullScreenMediaOrigin.FAVORITE
            dispatch(
                FullScreenMediaReducerEvent.Initialized(
                    mediaItems = mediaItems,
                    initialIndex = mediaItems.indexOfFirst { it.urlKey == args.url }.coerceAtLeast(0),
                    swipeEnabled = isFromFavorite,
                )
            )
            observeFavoriteUrls()
        }
    }

    private fun observeFavoriteUrls() {
        getFavoriteItemsUseCase()
            .map { items -> items.mapTo(HashSet(items.size)) { it.urlKey } }
            .distinctUntilChanged()
            .onEach { urls -> dispatch(FullScreenMediaReducerEvent.FavoritesChanged(urls)) }
            .launchIn(viewModelScope)
    }

    private fun resolveEmpty() {
        messageHelper.showOneButtonDialog(
            titleText = "",
            descText = resourceHelper.getString(StringResource.MEDIA_EMPTY),
            cantIgnore = true,
            onClickButton = { navigationHelper.navigateToBack() },
        )
        dispatch(FullScreenMediaReducerEvent.EmptyMediaResolved)
    }

    private fun addFavorite(item: MediaItemVO) {
        viewModelScope.launch {
            val favoriteItem = FavoriteItemVO(
                type = item.type,
                title = item.title,
                urlKey = item.urlKey,
                thumbnailUrl = item.thumbnailImageUrl,
                contentsImageUrl = item.contentsImageUrl,
                dateTime = item.dateTime,
            )
            registerFavoriteItemUseCase(favoriteItem)
        }
    }

    private fun deleteFavorite(url: String) {
        viewModelScope.launch {
            removeFavoriteItemUseCase(url)
        }
    }

    private fun clickBackNavigation() {
        navigationHelper.navigateToBack()
    }

    @AssistedFactory
    interface Factory {
        fun create(args: FullScreenMediaPage.Args): FullScreenMediaViewModel
    }
}
