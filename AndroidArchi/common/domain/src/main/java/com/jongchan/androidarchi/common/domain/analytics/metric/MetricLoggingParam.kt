package com.jongchan.androidarchi.common.domain.analytics.metric

sealed class MetricLoggingParam(val name: String) {

    data object AllFavoriteItemCount : MetricLoggingParam(name = "all_favorite_item_count")

    data object ItemImgUrl : MetricLoggingParam(name = "item_img_url")

    data object SearchKeyword : MetricLoggingParam(name = "search_keyword")
}
