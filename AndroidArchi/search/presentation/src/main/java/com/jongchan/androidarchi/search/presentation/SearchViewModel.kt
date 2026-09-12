package com.jongchan.androidarchi.search.presentation

import androidx.lifecycle.viewModelScope
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLogging
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingParam
import com.jongchan.androidarchi.common.domain.favorite.GetFavoriteItemsUseCase
import com.jongchan.androidarchi.common.domain.favorite.RegisterFavoriteItemUseCase
import com.jongchan.androidarchi.common.domain.favorite.RemoveFavoriteItemUseCase
import com.jongchan.androidarchi.common.domain.helper.LoggingHelper
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.mvi.MviViewModel
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaOrigin
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaPage
import com.jongchan.androidarchi.search.domain.SearchPageUseCase
import com.jongchan.androidarchi.search.domain.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchPageUseCase: SearchPageUseCase,
    private val searchUseCase: SearchUseCase,
    private val getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
    private val registerFavoriteItemUseCase: RegisterFavoriteItemUseCase,
    private val removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
    private val messageHelper: MessageHelper,
    private val navigationHelper: NavigationHelper,
    private val loggingHelper: LoggingHelper,
) : MviViewModel<SearchIntent, SearchUIState, SearchReducerEvent>(SearchUIState.empty) {
    private var searchJob: Job? = null
    private var searchedQuery: String = ""

    init {
        observeFavorites()
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> dispatch(SearchReducerEvent.QueryChanged(intent.query))
            is SearchIntent.Search -> search(intent.query)
            is SearchIntent.Clear -> clear()
            is SearchIntent.LoadNextPage -> loadNextPage()
            is SearchIntent.AddFavorite -> addFavorite(intent.url)
            is SearchIntent.DeleteFavorite -> deleteFavorite(intent.url)
            is SearchIntent.OpenFullScreen -> openFullScreen(intent.url)
        }
    }

    override fun reduce(state: SearchUIState, event: SearchReducerEvent): SearchUIState =
        when (event) {
            is SearchReducerEvent.PageInitLoaded -> state.setSDUIViews(event.result.sduiComponents)
            is SearchReducerEvent.QueryChanged -> state.copy(query = event.query)
            is SearchReducerEvent.Cleared -> SearchUIState.empty.copy(favoriteUrls = state.favoriteUrls)
            is SearchReducerEvent.SearchStarted -> state.copy(isLoading = true, hasSearched = true)
            is SearchReducerEvent.SearchFailed -> state.copy(isLoading = false)
            is SearchReducerEvent.SearchResultLoaded -> SearchUIState.fromSearchResult(
                state.sduiViewItems,
                state.query,
                event.result,
                state.favoriteUrls
            )

            is SearchReducerEvent.LoadMoreStarted -> state.copy(isLoadingMore = true)
            is SearchReducerEvent.MorePageLoaded -> state.appendPage(event.result, event.page)
            is SearchReducerEvent.LoadMoreFailed -> state.copy(isLoadingMore = false)
            is SearchReducerEvent.FavoritesChanged -> state.withFavorites(event.urls.asImmutable())
        }

    private fun observeFavorites() {
        getFavoriteItemsUseCase()
            .map { items -> items.mapTo(HashSet(items.size)) { it.urlKey } }
            .distinctUntilChanged()
            .onEach { urls -> dispatch(SearchReducerEvent.FavoritesChanged(urls)) }
            .launchIn(viewModelScope)
    }

    private fun search(query: String) {
        val trimmed = query.trim()
        dispatch(SearchReducerEvent.QueryChanged(query))
        if (trimmed.isEmpty()) return

        searchedQuery = trimmed
        searchJob?.cancel()
        dispatch(SearchReducerEvent.SearchStarted)
        searchJob = viewModelScope.launch {
            runCatching { searchUseCase(trimmed, SearchUIState.FIRST_PAGE) }
                .onSuccess { result ->
                    dispatch(SearchReducerEvent.SearchResultLoaded(result))
                }
                .onFailure {
                    dispatch(SearchReducerEvent.SearchFailed)
                    messageHelper.showSnackBar(messageRes = R.string.search_failed)
                }
        }
    }

    /**
     * 클리어 버튼: 진행 중인 검색을 취소하고 검색어·결과·페이징 상태를 초기값으로 되돌린다.
     * 즐겨찾기 캐시([SearchUIState.favoriteUrls])는 유지해 재검색 시 즐겨찾기 표시가 어긋나지 않게 한다.
     */
    private fun clear() {
        searchJob?.cancel()
        searchedQuery = ""
        dispatch(SearchReducerEvent.Cleared)
    }

    /**
     * 리스트 하단 도달 시 다음 페이지를 불러와 기존 리스트 뒤에 이어 붙인다.
     * 현재 페이지는 [SearchUIState.currentPage] 로 관리하며, 직전 검색어로만 재조회한다.
     * 이미 로딩 중이거나 마지막 페이지면 무시한다.
     */
    private fun loadNextPage() {
        val state = currentState
        if (!state.hasSearched || state.isLoading || state.isLoadingMore || state.isLastPage) return
        if (searchedQuery.isEmpty()) return

        val nextPage = state.currentPage + 1
        dispatch(SearchReducerEvent.LoadMoreStarted)
        searchJob = viewModelScope.launch {
            runCatching { searchUseCase(searchedQuery, nextPage) }
                .onSuccess { result ->
                    dispatch(
                        SearchReducerEvent.MorePageLoaded(
                            result,
                            nextPage
                        )
                    )
                }
                .onFailure {
                    dispatch(SearchReducerEvent.LoadMoreFailed)
                    messageHelper.showSnackBar(messageRes = R.string.search_failed)
                }
        }
    }

    private fun addFavorite(url: String) {
        viewModelScope.launch {
            val item = currentState.rawItemByUrl(url)
            if (item == null) {
                messageHelper.showSnackBar(messageRes = R.string.favorite_register_failed)
                return@launch
            }
            val favoriteItem = FavoriteItemVO(
                type = item.type,
                title = item.title,
                urlKey = item.urlKey,
                thumbnailUrl = item.thumbnailImageUrl,
                contentsImageUrl = item.contentsImageUrl,
                dateTime = item.dateTime,
            )
            registerFavoriteItemUseCase(favoriteItem).onSuccess {
                // 등록 직후 favoriteUrls 상태는 Flow 갱신 전일 수 있으므로 저장소에서 현재 즐겨찾기 수를 직접 읽는다.
                val allFavoriteItemCount = getFavoriteItemsUseCase().first().size
                loggingHelper.shotMetricLogging(
                    event = MetricLogging.ClickFavoriteToggleSearchPage,
                    params = mapOf(
                        MetricLoggingParam.AllFavoriteItemCount to allFavoriteItemCount,
                        MetricLoggingParam.ItemImgUrl to item.thumbnailImageUrl,
                        MetricLoggingParam.SearchKeyword to currentState.query,
                    ),
                )
            }
        }
    }

    private fun deleteFavorite(url: String) {
        viewModelScope.launch {
            removeFavoriteItemUseCase(url)
        }
    }

    private fun openFullScreen(url: String) {
        val target = currentState.rawItemByUrl(url) ?: return
        navigationHelper.navigateTo(
            FullScreenMediaPage.Args(
                origin = FullScreenMediaOrigin.SEARCH,
                url = target.urlKey,
                title = target.title,
                thumbnailImageUrl = target.thumbnailImageUrl,
                contentsImageUrl = target.contentsImageUrl,
                type = target.type,
            )
        )
    }

    init {
        viewModelScope.launch {
            runCatching { searchPageUseCase() }
                .onSuccess { result ->
                    dispatch(
                        SearchReducerEvent.PageInitLoaded(result)
                    )
                }
                .onFailure {
                    dispatch(SearchReducerEvent.LoadMoreFailed)
                    messageHelper.showSnackBar(messageRes = R.string.search_failed)
                }
        }
    }
}
