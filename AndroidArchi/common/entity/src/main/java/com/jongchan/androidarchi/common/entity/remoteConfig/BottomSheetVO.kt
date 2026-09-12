package com.jongchan.androidarchi.common.entity.remoteConfig

import kotlinx.serialization.Serializable

/**
 * Remote Config 에서 JSON 으로 내려오는 툴팁 바텀시트 정보.
 *
 * Remote Config 값은 DTO 를 거치지 않고 바로 이 VO 로 역직렬화되므로, 프로젝트 VO 규칙(비-nullable + 기본값)을
 * 그대로 지키면서 `@Serializable` 만 추가한다. 누락 필드는 기본값으로 채워진다(coerceInputValues).
 */
@Serializable
data class BottomSheetVO(
    val title: String = "",
    val description: String = "",
    val url: String = "",
) {
    companion object {
        val empty = BottomSheetVO()
    }
}
