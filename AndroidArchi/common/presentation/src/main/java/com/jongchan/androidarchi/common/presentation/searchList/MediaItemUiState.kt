package com.jongchan.androidarchi.common.presentation.searchList

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import com.jongchan.androidarchi.common.entity.ai.MediaFeedbackType
import com.jongchan.androidarchi.common.entity.media.MediaType
import com.jongchan.androidarchi.common.presentation.R

/**
 * 검색 결과 리스트 셀 1건의 표시용 상태.
 *
 * - [title]           : display_sitename(이미지) / title(동영상)
 * - [contentsUrl]     : image_url(이미지) / url(동영상). 항목 고유 식별 키 겸 상세 진입 키.
 * - [descriptionText] : doc_url(이미지) / url(동영상). 화면에 표시하는 주소.
 * - [collection]      : 이미지 전용 카테고리. 동영상/없음은 빈 문자열.
 * - [dateTimeText]    : 화면에 바로 그릴 수 있도록 포맷팅된 날짜 문자열.
 * - [page]            : 이 항목이 속한 검색 페이지(1-based). 페이지 구분자 렌더링에 사용.
 * - [isFavorite]      : 즐겨찾기 여부. ViewModel 이 즐겨찾기 url 집합을 접어 셋팅한다.
 * - [feedbackType]    : 온디바이스 AI 취향 평가(Like / Dislike / None) 상태.
 * - [preferenceScore] : 온디바이스 AI(MediaPipe)가 계산한 유저 취향 매칭 점수(0~100%). 피드백 이력이 없으면 null.
 *
 * 아이콘 res 분기는 이 상태가 끝내고, 하트 tint 는 테마(contentFavorite)에서 가져온다.
 */
@Stable
data class MediaItemUiState(
    val mediaType: MediaType,
    val title: String,
    val descriptionText: String = "",
    val thumbnailUrl: String,
    val contentsUrl: String,
    val collection: String = "",
    val dateTimeText: String = "",
    val page: Int = 1,
    val isFavorite: Boolean = false,
    val feedbackType: MediaFeedbackType = MediaFeedbackType.NONE,
    val preferenceScore: Int? = null,
) {
    @get:DrawableRes
    val mediaTypeIconRes: Int
        get() = if (mediaType == MediaType.VIDEO) R.drawable.ic_video_24 else R.drawable.ic_image_24

    @get:DrawableRes
    val favoriteIconRes: Int
        get() = if (isFavorite) R.drawable.ic_like_on_24 else R.drawable.ic_like_off_24
}
