package com.jongchan.androidarchi.common.data.analytics.metric.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Apps Script `doPost(e)` 가 받는 요청 본문.
 *
 * ```json
 * { "token": "...", "event": "click_favorite_toggle_search_page",
 *   "params": { "search_keyword": "고양이", ... } }
 * ```
 * 스크립트는 `event` 이름의 시트 탭을 찾아 1행 헤더 순서대로 `params[header]` 를 append 한다.
 * `timestamp` 컬럼은 앱이 보내지 않고 스크립트가 실행 시각(`new Date()`)으로 채운다.
 */
@Serializable
data class MetricLogRequestDTO(
    val token: String? = null,
    val event: String? = null,
    val params: Map<String, JsonElement>? = null,
)

/** Apps Script 응답. HTTP 200 이어도 `ok=false` 일 수 있다(토큰 불일치, 탭 없음 등). */
@Serializable
data class MetricLogResponseDTO(
    val ok: Boolean? = null,
    val error: String? = null,
)
