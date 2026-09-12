package com.jongchan.androidarchi.common.data.analytics.metric

import com.jongchan.androidarchi.common.data.analytics.metric.dto.MetricLogRequestDTO
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLogging
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingParam
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingRepository
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class MetricLoggingRepositoryImpl(
    private val dataSource: MetricLoggingDataSource,
    private val token: String,
) : MetricLoggingRepository {

    override suspend fun send(
        event: MetricLogging,
        params: Map<out MetricLoggingParam, Any?>,
    ): Boolean {
        val request = MetricLogRequestDTO(
            token = token,
            event = event.name,
            params = params.entries.associate { (param, value) -> param.name to value.toJsonElement() },
        )
        val response = dataSource.send(request)
        if (response.ok != true) {
            throw IllegalStateException("Metric logging rejected by server: ${response.error ?: "unknown"}")
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
}
