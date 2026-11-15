package com.jongchan.androidarchi.favorite.presentation

import com.jongchan.androidarchi.common.presentation.mvi.MviIntent

sealed interface FavoriteIntent : MviIntent {
    data object Load : FavoriteIntent
    data class DeleteFavorite(val url: String) : FavoriteIntent
    data class OpenFullScreen(val url: String) : FavoriteIntent
    data object RefreshTasteReport : FavoriteIntent
}
