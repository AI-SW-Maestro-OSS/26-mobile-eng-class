package com.jongchan.androidarchi.favorite.presentation

import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.presentation.mvi.ReducerEvent
import com.jongchan.androidarchi.favorite.domain.FavoriteTasteReportVO

sealed interface FavoriteReducerEvent : ReducerEvent {
    data object LoadingStarted : FavoriteReducerEvent
    data class ItemsLoaded(val items: List<FavoriteItemVO>) : FavoriteReducerEvent
    data object TasteReportLoadingStarted : FavoriteReducerEvent
    data class TasteReportLoaded(val report: FavoriteTasteReportVO) : FavoriteReducerEvent
}
