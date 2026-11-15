package com.jongchan.androidarchi.search.presentation

import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.presentation.mvi.MviIntent

sealed interface SearchIntent : MviIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data class Search(val query: String) : SearchIntent
    data object Clear : SearchIntent
    data object LoadNextPage : SearchIntent
    data class AddFavorite(val url: String) : SearchIntent
    data class DeleteFavorite(val url: String) : SearchIntent
    data class OpenFullScreen(val url: String) : SearchIntent
    data class ToggleFeedback(val url: String, val targetType: MediaFeedbackType) : SearchIntent
    data object ToggleAiSort : SearchIntent
    data object ClearFeedbacks : SearchIntent
}
