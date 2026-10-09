package com.jongchan.androidarchi.common.data.analytics.tti.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Apps Script `doPost(e)` 가 받는 요청 본문 (기존 `LoggingBucket` 배포를 그대로 사용).
 *
 * ```json
 * { "token": "...", "event": "tti_report",
 *   "params": { "page_name": "FullScreenMediaPage", "tti_time": 1234, ... } }
 * ```
 * 범용 스크립트가 `event` 이름(`tti_report`)의 탭을 찾아 append 하므로 스크립트 수정 없이 탭만 추가하면 된다.
 * `timestamp` 컬럼은 앱이 보내지 않고 스크립트가 실행 시각(`new Date()`)으로 채운다.
 */
@Serializable
data class TtiLogRequestDTO(
    val token: String? = null,
    val event: String? = null,
    val params: Map<String, JsonElement>? = null,
)

@Serializable
data class TtiLogResponseDTO(
    val ok: Boolean? = null,
    val error: String? = null,
)
