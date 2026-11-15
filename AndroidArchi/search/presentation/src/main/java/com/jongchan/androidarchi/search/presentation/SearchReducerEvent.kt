package com.jongchan.androidarchi.search.presentation

import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.media.MediaSearchResultVO
import com.jongchan.androidarchi.common.presentation.mvi.ReducerEvent
import com.jongchan.androidarchi.search.domain.SearchPageVO
import com.jongchan.androidarchi.search.domain.SearchQuerySuggestionVO
import kotlinx.collections.immutable.ImmutableMap

sealed interface SearchReducerEvent : ReducerEvent {
    data class PageInitLoaded(val result: SearchPageVO) : SearchReducerEvent
    data class QueryChanged(val query: String) : SearchReducerEvent
    data object Cleared : SearchReducerEvent
    data object SearchStarted : SearchReducerEvent
    data object SearchFailed : SearchReducerEvent
    data class SearchResultLoaded(
        val result: MediaSearchResultVO,
        val preferenceScores: ImmutableMap<String, Int>,
    ) : SearchReducerEvent
    data object LoadMoreStarted : SearchReducerEvent
    data class MorePageLoaded(
        val result: MediaSearchResultVO,
        val page: Int,
        val preferenceScores: ImmutableMap<String, Int>,
    ) : SearchReducerEvent
    data object LoadMoreFailed : SearchReducerEvent
    data class FavoritesChanged(val urls: Set<String>) : SearchReducerEvent
    data class PreferenceUpdated(
        val feedbackMap: ImmutableMap<String, MediaFeedbackType>,
        val preferenceScores: ImmutableMap<String, Int>,
        val likedCount: Int,
        val dislikedCount: Int,
    ) : SearchReducerEvent
    data object AiSortToggled : SearchReducerEvent
    data object QuerySuggestionLoadingStarted : SearchReducerEvent
    data class QuerySuggestionLoaded(val suggestion: SearchQuerySuggestionVO) : SearchReducerEvent
}
