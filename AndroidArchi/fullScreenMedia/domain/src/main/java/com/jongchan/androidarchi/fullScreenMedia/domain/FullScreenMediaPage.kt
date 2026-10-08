package com.jongchan.androidarchi.fullScreenMedia.domain

import com.jongchan.androidarchi.common.domain.navigation.NavRoute
import com.jongchan.androidarchi.common.domain.navigation.Page
import com.jongchan.androidarchi.common.entity.media.MediaType

object FullScreenMediaPage {
    const val PATH = "/fullScreenMedia"

    private const val KEY_ORIGIN = "origin"
    private const val KEY_URL = "url"
    private const val KEY_TITLE = "title"
    private const val KEY_THUMBNAIL_IMAGE_URL = "thumbnailImageUrl"
    private const val KEY_CONTENTS_IMAGE_URL = "contentsImageUrl"
    private const val KEY_TYPE = "type"

    /**
     * FullScreen 화면이 요구하는 인자.
     *
     * 무거운 미디어 리스트를 직렬화해 넘기지 않고, 진입 경로([origin])와 식별에 필요한 최소 정보만 전달한다.
     * 실제 노출할 [com.jongchan.androidarchi.common.entity.media.MediaItemVO] 리스트는
     * [GetFullScreenMediaItemsUseCase] 가 [origin] 에 맞춰 구성한다.
     * - [FullScreenMediaOrigin.FAVORITE] : [url] 로 즐겨찾기 목록에서 시작 위치를 찾고 좌우 스와이프 페이징.
     * - [FullScreenMediaOrigin.SEARCH]   : [url]/[title]/[thumbnailImageUrl]/[contentsImageUrl]/[type] 로 단일 항목만 노출.
     * - [FullScreenMediaOrigin.DEEP_LINK]: [url]/[title] 로 단일 항목 노출(타입 미지정 시 UNKNOWN).
     *
     * [contentsImageUrl] 이 풀스크린에 띄울 이미지(이미지=원본, 동영상=썸네일). 비어있으면 [url] 로 폴백한다.
     */
    data class Args(
        val origin: FullScreenMediaOrigin = FullScreenMediaOrigin.DEEP_LINK,
        val url: String = "",
        val title: String = "",
        val thumbnailImageUrl: String = "",
        val contentsImageUrl: String = "",
        val type: MediaType = MediaType.UNKNOWN,
    ) : Page {
        override fun toRoute(): NavRoute {
            val argsMap = buildMap {
                put(KEY_ORIGIN, origin.name)
                if (url.isNotEmpty()) put(KEY_URL, url)
                if (title.isNotEmpty()) put(KEY_TITLE, title)
                if (thumbnailImageUrl.isNotEmpty()) put(KEY_THUMBNAIL_IMAGE_URL, thumbnailImageUrl)
                if (contentsImageUrl.isNotEmpty()) put(KEY_CONTENTS_IMAGE_URL, contentsImageUrl)
                if (type != MediaType.UNKNOWN) put(KEY_TYPE, type.name)
            }
            return NavRoute(PATH, argsMap)
        }

        companion object {
            /** NavRoute.args 로부터 typed Args 복원. 알 수 없는 값은 안전한 기본값으로 대체한다. */
            fun from(args: Map<String, String>): Args = Args(
                origin = args[KEY_ORIGIN]
                    ?.let { name -> runCatching { FullScreenMediaOrigin.valueOf(name) }.getOrNull() }
                    ?: FullScreenMediaOrigin.DEEP_LINK,
                url = args[KEY_URL].orEmpty(),
                title = args[KEY_TITLE].orEmpty(),
                thumbnailImageUrl = args[KEY_THUMBNAIL_IMAGE_URL].orEmpty(),
                contentsImageUrl = args[KEY_CONTENTS_IMAGE_URL].orEmpty(),
                type = args[KEY_TYPE]
                    ?.let { name -> runCatching { MediaType.valueOf(name) }.getOrNull() }
                    ?: MediaType.UNKNOWN,
            )
        }
    }
}
