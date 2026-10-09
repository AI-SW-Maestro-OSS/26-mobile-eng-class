package com.jongchan.androidarchi.common.data.analytics.tti

import com.jongchan.androidarchi.common.data.analytics.tti.dto.TtiLogRequestDTO
import com.jongchan.androidarchi.common.domain.analytics.tti.TtiLoggingRepository
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class TtiLoggingRepositoryImpl(
    private val dataSource: TtiLoggingDataSource,
    private val token: String,
) : TtiLoggingRepository {

    override suspend fun send(report: Map<String, Any?>): Boolean {
        val request = TtiLogRequestDTO(
            token = token,
            event = TTI_REPORT_EVENT,
            params = report.entries.associate { (name, value) -> name to value.toJsonElement() },
        )
        val response = dataSource.send(request)
        if (response.ok != true) {
            throw IllegalStateException("TTI logging rejected by server: ${response.error ?: "unknown"}")
        }
        return true
    }

    /** 시트 셀에 그대로 들어갈 수 있는 원시 타입만 JSON 원시값으로, 그 외는 문자열로 보낸다. */
    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        else -> JsonPrimitive(toString())
    }

    companion object {
        /** TTI 시트의 탭 이름. 범용 Apps Script 가 이 이름의 탭을 찾아 append 한다. */
        const val TTI_REPORT_EVENT = "tti_report"
    }
}
