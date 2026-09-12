package com.jongchan.androidarchi.common.domain.analytics.metric

enum class MetricEventType {
    Track, Exposure,
}

sealed class MetricLogging(
    val name: String,
    val type: MetricEventType,
    val availableParams: Set<MetricLoggingParam>
) {
    data object ClickFavoriteToggleSearchPage : MetricLogging(
        name = "click_favorite_toggle_search_page",
        type = MetricEventType.Track,
        availableParams = setOf(
            MetricLoggingParam.AllFavoriteItemCount,
            MetricLoggingParam.ItemImgUrl,
            MetricLoggingParam.SearchKeyword,
        ),
    )
}
