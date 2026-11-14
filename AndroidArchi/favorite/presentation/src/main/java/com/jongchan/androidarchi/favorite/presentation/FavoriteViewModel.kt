package com.jongchan.androidarchi.favorite.presentation

import androidx.lifecycle.viewModelScope
import com.jongchan.androidarchi.common.domain.favorite.GetFavoriteItemsUseCase
import com.jongchan.androidarchi.common.domain.favorite.RemoveFavoriteItemUseCase
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.store.GlobalState
import com.jongchan.androidarchi.common.domain.store.GlobalStore
import com.jongchan.androidarchi.common.domain.store.SampleState
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.presentation.mvi.MviViewModel
import com.jongchan.androidarchi.favorite.domain.FavoriteTasteReportUseCase
import com.jongchan.androidarchi.favorite.domain.FavoriteTasteReportVO
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaOrigin
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    globalStore: GlobalStore,
    private val getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
    private val removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
    private val favoriteTasteReportUseCase: FavoriteTasteReportUseCase,
    private val navigationHelper: NavigationHelper,
    private val messageHelper: MessageHelper,
) : MviViewModel<FavoriteIntent, FavoriteUIState, FavoriteReducerEvent>(
    FavoriteUIState.empty,
    globalStore,
    observedStateTypes = setOf(SampleState::class)
) {
    private var tasteReportJob: Job? = null

    init {
        onIntent(FavoriteIntent.Load)
    }

    override fun onIntent(intent: FavoriteIntent) {
        when (intent) {
            FavoriteIntent.Load -> observeFavorites()
            is FavoriteIntent.DeleteFavorite -> deleteFavorite(intent.url)
            is FavoriteIntent.OpenFullScreen -> openFullScreen(intent.url)
            FavoriteIntent.RefreshTasteReport -> generateTasteReport(currentState.rawFavoriteItems)
        }
    }

    override fun reduce(state: FavoriteUIState, event: FavoriteReducerEvent): FavoriteUIState =
        when (event) {
            FavoriteReducerEvent.LoadingStarted -> state.copy(isLoading = true)
            is FavoriteReducerEvent.ItemsLoaded -> state.withItems(event.items)
            FavoriteReducerEvent.TasteReportLoadingStarted -> state.copy(isAnalyzingTaste = true)
            is FavoriteReducerEvent.TasteReportLoaded -> state.withTasteReport(event.report)
        }

    override fun observeGlobalState(state: GlobalState) {
        when (state) {
            is SampleState -> {
                messageHelper.showToast(state.msg)
            }
        }
    }

    private fun observeFavorites() {
        dispatch(FavoriteReducerEvent.LoadingStarted)
        getFavoriteItemsUseCase()
            .onEach { items ->
                dispatch(FavoriteReducerEvent.ItemsLoaded(items))
                generateTasteReport(items)
            }
            .launchIn(viewModelScope)
    }

    private fun generateTasteReport(items: List<FavoriteItemVO>) {
        tasteReportJob?.cancel()
        if (items.isEmpty()) {
            dispatch(FavoriteReducerEvent.TasteReportLoaded(FavoriteTasteReportVO.empty))
            return
        }
        dispatch(FavoriteReducerEvent.TasteReportLoadingStarted)
        tasteReportJob = viewModelScope.launch {
            val report = favoriteTasteReportUseCase(items)
            dispatch(FavoriteReducerEvent.TasteReportLoaded(report))
        }
    }

    private fun deleteFavorite(url: String) {
        viewModelScope.launch {
            removeFavoriteItemUseCase(url)
        }
    }

    private fun openFullScreen(url: String) {
        navigationHelper.navigateTo(
            FullScreenMediaPage.Args(
                origin = FullScreenMediaOrigin.FAVORITE,
                url = url,
            )
        )
    }
}
