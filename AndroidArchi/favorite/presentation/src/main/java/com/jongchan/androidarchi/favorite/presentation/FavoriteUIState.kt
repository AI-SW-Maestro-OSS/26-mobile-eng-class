package com.jongchan.androidarchi.favorite.presentation

import com.jongchan.androidarchi.common.entity.ai.AIEngineType
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.presentation.mvi.UiState
import com.jongchan.androidarchi.common.presentation.searchList.MediaItemUiState
import com.jongchan.androidarchi.common.presentation.searchList.formatKakaoDateTime
import com.jongchan.androidarchi.favorite.domain.FavoriteTasteReportVO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class FavoriteUIState(
    val isLoading: Boolean,
    val favoriteItemList: ImmutableList<MediaItemUiState>,
    val rawFavoriteItems: ImmutableList<FavoriteItemVO> = persistentListOf(),
    val isAnalyzingTaste: Boolean = false,
    val tasteSummary: String = "",
    val tasteKeywords: ImmutableList<String> = persistentListOf(),
    val tasteEngineType: AIEngineType = AIEngineType.LOCAL_FALLBACK,
) : UiState {

    fun withItems(items: List<FavoriteItemVO>): FavoriteUIState = copy(
        isLoading = false,
        rawFavoriteItems = items.toImmutableList(),
        favoriteItemList = items.map { it.toUiState() }.toImmutableList(),
        tasteSummary = if (items.isEmpty()) "" else tasteSummary,
        tasteKeywords = if (items.isEmpty()) persistentListOf() else tasteKeywords,
    )

    fun withTasteReport(report: FavoriteTasteReportVO): FavoriteUIState = copy(
        isAnalyzingTaste = false,
        tasteSummary = report.summary,
        tasteKeywords = report.tasteKeywords.toImmutableList(),
        tasteEngineType = report.engineType,
    )

    companion object {
        val empty = FavoriteUIState(
            isLoading = true,
            favoriteItemList = persistentListOf(),
            rawFavoriteItems = persistentListOf(),
            isAnalyzingTaste = false,
            tasteSummary = "",
            tasteKeywords = persistentListOf(),
            tasteEngineType = AIEngineType.LOCAL_FALLBACK,
        )

        private fun FavoriteItemVO.toUiState(): MediaItemUiState = MediaItemUiState(
            mediaType = type,
            title = title,
            thumbnailUrl = thumbnailUrl,
            contentsUrl = urlKey,
            dateTimeText = formatKakaoDateTime(dateTime),
            isFavorite = true,
        )
    }
}
