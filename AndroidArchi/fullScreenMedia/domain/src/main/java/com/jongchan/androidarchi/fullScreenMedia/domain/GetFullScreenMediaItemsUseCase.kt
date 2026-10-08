package com.jongchan.androidarchi.fullScreenMedia.domain

import com.jongchan.androidarchi.common.domain.base.BaseUseCase
import com.jongchan.androidarchi.common.domain.favorite.FavoriteRepository
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.entity.favorite.FavoriteItemVO
import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.tti.TTIHelper
import com.jongchan.androidarchi.tti.TimelineCategory
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 진입 경로([FullScreenMediaPage.Args.origin])에 맞춰 풀스크린에 노출할 미디어 리스트를 구성한다.
 *
 * TTI 골든 예제: 이 화면은 실제 API 호출이 없지만(로컬 즐겨찾기 / 전달받은 인자로 구성),
 * API 가 있는 화면에서 UseCase 가 API 구간을 어떻게 찍는지 보여주기 위해 데이터 로드를 API 호출처럼 감싼다.
 * [ttiHelper] 는 ViewModel 과 같은 인스턴스(@ViewModelScoped)라 별도 전달 없이 같은 TTI 측정에 기록된다.
 */
class GetFullScreenMediaItemsUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    resourceHelper: ResourceHelper,
    messageHelper: MessageHelper,
    navigationHelper: NavigationHelper,
    ttiHelper: TTIHelper,
) : BaseUseCase(resourceHelper, messageHelper, navigationHelper, ttiHelper) {

    suspend operator fun invoke(args: FullScreenMediaPage.Args): List<MediaItemVO> {
        // API_REQUEST_READY_TIME: 요청 파라미터 구성 구간 (API 화면이라면 query/page 등 요청 객체를 만드는 자리)
        ttiHelper.startTTITimeline(TimelineCategory.API_REQUEST_READY_TIME)
        val origin = args.origin
        ttiHelper.endTTITimeline(TimelineCategory.API_REQUEST_READY_TIME)

        // API_RESPONSE_TIME: 호출 ~ 응답 수신 구간 (API 화면이라면 repository.getXxx() 호출을 감싸는 자리)
        // 실패(예외) 시 end 를 찍지 않으므로 해당 측정은 미완료(is_bounced / timeout)로 리포트된다.
        ttiHelper.startTTITimeline(TimelineCategory.API_RESPONSE_TIME)
        val mediaItems = when (origin) {
            FullScreenMediaOrigin.FAVORITE ->
                favoriteRepository.getFavoriteItemsFlow().first().map { it.toMediaItemVO() }

            FullScreenMediaOrigin.SEARCH,
            FullScreenMediaOrigin.DEEP_LINK -> buildSingleItem(args)
        }
        ttiHelper.endTTITimeline(TimelineCategory.API_RESPONSE_TIME)
        return mediaItems
    }

    private fun buildSingleItem(args: FullScreenMediaPage.Args): List<MediaItemVO> {
        if (args.url.isBlank()) return emptyList()
        return listOf(
            MediaItemVO(
                type = args.type,
                title = args.title,
                urlKey = args.url,
                thumbnailImageUrl = args.thumbnailImageUrl,
                contentsImageUrl = args.contentsImageUrl.ifBlank { args.url },
            )
        )
    }
}

private fun FavoriteItemVO.toMediaItemVO(): MediaItemVO = MediaItemVO(
    type = type,
    title = title,
    urlKey = urlKey,
    thumbnailImageUrl = thumbnailUrl,
    // 저장된 contentsImageUrl 을 우선 사용. 구버전 저장본(빈 값)은 이미지=원본(urlKey), 동영상=썸네일로 폴백.
    contentsImageUrl = contentsImageUrl.ifBlank { if (type == MediaType.VIDEO) thumbnailUrl else urlKey },
    dateTime = dateTime,
)
