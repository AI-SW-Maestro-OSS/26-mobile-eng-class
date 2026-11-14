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
import com.jongchan.androidarchi.common.domain.store.GlobalStore
import com.jongchan.androidarchi.common.domain.store.SampleAction
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.ai.MediaPreferenceFeedbackVO
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.presentation.R
import com.jongchan.androidarchi.common.presentation.mvi.MviViewModel
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaOrigin
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaPage
import com.jongchan.androidarchi.search.domain.SearchPageUseCase
import com.jongchan.androidarchi.search.domain.SearchPreferenceUseCase
import com.jongchan.androidarchi.search.domain.SearchQuerySuggestionUseCase
import com.jongchan.androidarchi.search.domain.SearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableMap
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
    globalStore: GlobalStore,
    private val searchPageUseCase: SearchPageUseCase,
    private val searchUseCase: SearchUseCase,
    private val searchPreferenceUseCase: SearchPreferenceUseCase,
    private val searchQuerySuggestionUseCase: SearchQuerySuggestionUseCase,
    private val getFavoriteItemsUseCase: GetFavoriteItemsUseCase,
    private val registerFavoriteItemUseCase: RegisterFavoriteItemUseCase,
    private val removeFavoriteItemUseCase: RemoveFavoriteItemUseCase,
    private val messageHelper: MessageHelper,
    private val navigationHelper: NavigationHelper,
    private val loggingHelper: LoggingHelper,
) : MviViewModel<SearchIntent, SearchUIState, SearchReducerEvent>(
    SearchUIState.empty,
    globalStore
) {
    private var searchJob: Job? = null
    private var suggestionJob: Job? = null
    private var searchedQuery: String = ""

    init {
        observeFavorites()
        observePreferenceFeedbacks()
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
            is SearchIntent.ToggleFeedback -> toggleFeedback(intent.url, intent.targetType)
            is SearchIntent.ToggleAiSort -> toggleAiSort()
            is SearchIntent.ClearFeedbacks -> clearFeedbacks()
        }
    }

    override fun reduce(state: SearchUIState, event: SearchReducerEvent): SearchUIState =
        when (event) {
            is SearchReducerEvent.PageInitLoaded -> state.setSDUIViews(event.result.sduiComponents)
            is SearchReducerEvent.QueryChanged -> state.copy(query = event.query)
            is SearchReducerEvent.Cleared -> SearchUIState.empty.copy(
                favoriteUrls = state.favoriteUrls,
                feedbackMap = state.feedbackMap,
                likedCount = state.likedCount,
                dislikedCount = state.dislikedCount,
                isAiSortEnabled = state.isAiSortEnabled,
            )

            is SearchReducerEvent.SearchStarted -> state.copy(isLoading = true, hasSearched = true)
            is SearchReducerEvent.SearchFailed -> state.copy(isLoading = false)
            is SearchReducerEvent.SearchResultLoaded -> SearchUIState.fromSearchResult(
                previousState = state,
                result = event.result,
                preferenceScores = event.preferenceScores,
            )

            is SearchReducerEvent.LoadMoreStarted -> state.copy(isLoadingMore = true)
            is SearchReducerEvent.MorePageLoaded -> state.appendPage(
                result = event.result,
                page = event.page,
                newPreferenceScores = event.preferenceScores,
            )

            is SearchReducerEvent.LoadMoreFailed -> state.copy(isLoadingMore = false)
            is SearchReducerEvent.FavoritesChanged -> state.withFavorites(event.urls.asImmutable())
            is SearchReducerEvent.PreferenceUpdated -> state.withPreferences(
                newFeedbackMap = event.feedbackMap,
                newPreferenceScores = event.preferenceScores,
                newLikedCount = event.likedCount,
                newDislikedCount = event.dislikedCount,
            )

            is SearchReducerEvent.AiSortToggled -> state.toggleAiSort()
            is SearchReducerEvent.QuerySuggestionLoadingStarted -> state.copy(
                isGeneratingSuggestion = true,
            )

            is SearchReducerEvent.QuerySuggestionLoaded -> state.withQuerySuggestion(event.suggestion)
        }

    private fun observeFavorites() {
        getFavoriteItemsUseCase()
            .map { items -> items.mapTo(HashSet(items.size)) { it.urlKey } }
            .distinctUntilChanged()
            .onEach { urls -> dispatch(SearchReducerEvent.FavoritesChanged(urls)) }
            .launchIn(viewModelScope)
    }

    private fun observePreferenceFeedbacks() {
        searchPreferenceUseCase.observeFeedbacks()
            .distinctUntilChanged()
            .onEach { feedbacks ->
                val feedbackMap = feedbacks
                    .associate { it.urlKey to it.feedbackType }
                    .toImmutableMap()
                val likedCount = feedbacks.count { it.feedbackType == MediaFeedbackType.LIKE }
                val dislikedCount = feedbacks.count { it.feedbackType == MediaFeedbackType.DISLIKE }
                val rawItems = currentState.rawMediaItems
                val scores = if (rawItems.isNotEmpty() && feedbacks.isNotEmpty()) {
                    searchPreferenceUseCase.evaluateScores(rawItems).toImmutableMap()
                } else {
                    emptyMap<String, Int>().toImmutableMap()
                }
                dispatch(
                    SearchReducerEvent.PreferenceUpdated(
                        feedbackMap = feedbackMap,
                        preferenceScores = scores,
                        likedCount = likedCount,
                        dislikedCount = dislikedCount,
                    )
                )
                if (searchedQuery.isNotEmpty() && rawItems.isNotEmpty()) {
                    refreshQuerySuggestions(searchedQuery, rawItems, feedbacks)
                }
            }
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
            globalStore.dispatch(SampleAction("Recent query words : $query"))
            runCatching { searchUseCase(trimmed, SearchUIState.FIRST_PAGE) }
                .onSuccess { result ->
                    val scores = searchPreferenceUseCase.evaluateScores(result.items).toImmutableMap()
                    dispatch(SearchReducerEvent.SearchResultLoaded(result, scores))
                    refreshQuerySuggestions(trimmed, result.items)
                }
                .onFailure {
                    dispatch(SearchReducerEvent.SearchFailed)
                    messageHelper.showSnackBar(messageRes = R.string.search_failed)
                }
        }
    }

    private fun refreshQuerySuggestions(
        query: String,
        items: List<MediaItemVO>,
        feedbacksOverride: List<MediaPreferenceFeedbackVO>? = null,
    ) {
        suggestionJob?.cancel()
        dispatch(SearchReducerEvent.QuerySuggestionLoadingStarted)
        suggestionJob = viewModelScope.launch {
            val suggestion = searchQuerySuggestionUseCase(
                query = query,
                items = items,
                feedbacksOverride = feedbacksOverride,
            )
            dispatch(SearchReducerEvent.QuerySuggestionLoaded(suggestion))
        }
    }

    private fun clear() {
        searchJob?.cancel()
        suggestionJob?.cancel()
        searchedQuery = ""
        dispatch(SearchReducerEvent.Cleared)
    }

    private fun loadNextPage() {
        val state = currentState
        if (!state.hasSearched || state.isLoading || state.isLoadingMore || state.isLastPage) return
        if (searchedQuery.isEmpty()) return

        val nextPage = state.currentPage + 1
        dispatch(SearchReducerEvent.LoadMoreStarted)
        searchJob = viewModelScope.launch {
            runCatching { searchUseCase(searchedQuery, nextPage) }
                .onSuccess { result ->
                    val scores = searchPreferenceUseCase.evaluateScores(result.items).toImmutableMap()
                    dispatch(
                        SearchReducerEvent.MorePageLoaded(
                            result = result,
                            page = nextPage,
                            preferenceScores = scores,
                        )
                    )
                }
                .onFailure {
                    dispatch(SearchReducerEvent.LoadMoreFailed)
                    messageHelper.showSnackBar(messageRes = R.string.search_failed)
                }
        }
    }

    private fun toggleFeedback(url: String, targetType: MediaFeedbackType) {
        val item = currentState.rawItemByUrl(url) ?: return
        val currentType = currentState.feedbackMap[url] ?: MediaFeedbackType.NONE
        val nextType = if (currentType == targetType) MediaFeedbackType.NONE else targetType
        viewModelScope.launch {
            searchPreferenceUseCase.recordFeedback(item, nextType)
        }
    }

    private fun toggleAiSort() {
        val state = currentState
        if (!state.hasFeedbackHistory && !state.isAiSortEnabled) {
            messageHelper.showSnackBar(messageRes = R.string.ai_feedback_need_rating)
            return
        }
        dispatch(SearchReducerEvent.AiSortToggled)
    }

    private fun clearFeedbacks() {
        viewModelScope.launch {
            searchPreferenceUseCase.clearFeedbacks()
            messageHelper.showSnackBar(messageRes = R.string.ai_feedback_cleared)
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
