package com.jongchan.androidarchi.fullScreenMedia.domain.tti

import com.jongchan.androidarchi.tti.TTIPage
import com.jongchan.androidarchi.tti.TimelineCategory

// 실제 API 는 없지만 골든 예제로서 API 구간도 측정한다.
// - API 구간: GetFullScreenMediaItemsUseCase (UseCase 에서 찍는 패턴)
// - 뷰·이미지 구간: FullScreenMediaFragment (View 에서 찍는 패턴)
// 마지막 타임라인(IMAGE_LOADED_TIME)이 완성되어야 endTTITracking 이 받아들여진다.
object FullScreenMediaTTIPage : TTIPage {
    override val pageName = "fullScreenMediaPage"
    override val timelines = listOf(
        TimelineCategory.TTI_TIME,
        TimelineCategory.VIEW_CREATION_TIME,
        TimelineCategory.API_REQUEST_READY_TIME,
        TimelineCategory.API_RESPONSE_TIME,
        TimelineCategory.VIEW_BINDING_TIME,
        TimelineCategory.IMAGE_LOADED_TIME,
    )
}
