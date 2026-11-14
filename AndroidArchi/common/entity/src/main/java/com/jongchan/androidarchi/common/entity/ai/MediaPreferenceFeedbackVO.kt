package com.jongchan.androidarchi.common.entity.ai

import com.jongchan.androidarchi.common.entity.media.MediaItemVO
import com.jongchan.androidarchi.common.entity.media.MediaType

/**
 * 유저가 미디어 아이템에 남긴 취향 피드백 상태.
 */
enum class MediaFeedbackType {
    NONE,
    LIKE,
    DISLIKE,
}

/**
 * 온디바이스 AI 취향 학습(Rocchio 선호 벡터 계산)에 사용되는 유저 피드백 VO.
 *
 * - [urlKey]      : 미디어 고유 식별 키 (`MediaItemVO.urlKey`)
 * - [title]       : 미디어 타이틀
 * - [collection]  : 카테고리 (`blog`, `news`, `cafe` 등)
 * - [description] : 출처 URL 또는 설명 텍스트
 * - [mediaType]   : 미디어 타입 (`IMAGE`, `VIDEO`)
 * - [feedbackType]: 유저 평가 (`LIKE`, `DISLIKE`, `NONE`)
 * - [updatedAt]   : 피드백 시각 (epoch millis)
 */
data class MediaPreferenceFeedbackVO(
    val urlKey: String = "",
    val title: String = "",
    val collection: String = "",
    val description: String = "",
    val mediaType: MediaType = MediaType.UNKNOWN,
    val feedbackType: MediaFeedbackType = MediaFeedbackType.NONE,
    val updatedAt: Long = 0L,
) {
    val semanticText: String
        get() = buildSemanticText(
            mediaType = mediaType,
            collection = collection,
            title = title,
            description = description,
        )

    companion object {
        val empty: MediaPreferenceFeedbackVO = MediaPreferenceFeedbackVO()
    }
}

/**
 * [MediaItemVO] 에서 MediaPipe 임베딩 추론에 사용할 대표 시맨틱 텍스트를 생성한다.
 */
fun MediaItemVO.toSemanticText(): String = buildSemanticText(
    mediaType = type,
    collection = collection,
    title = title,
    description = pageLinkUrl,
)

internal fun buildSemanticText(
    mediaType: MediaType,
    collection: String,
    title: String,
    description: String,
): String {
    val hostOrPath = description
        .removePrefix("https://")
        .removePrefix("http://")
        .substringBefore("?")
        .replace('/', ' ')
        .replace('.', ' ')
        .replace('-', ' ')
        .replace('_', ' ')
    return buildString {
        if (mediaType != MediaType.UNKNOWN) {
            append(mediaType.name.lowercase()).append(' ')
        }
        if (collection.isNotBlank()) {
            append(collection).append(' ')
        }
        if (title.isNotBlank()) {
            append(title).append(' ')
        }
        if (hostOrPath.isNotBlank()) {
            append(hostOrPath)
        }
    }.trim()
}
