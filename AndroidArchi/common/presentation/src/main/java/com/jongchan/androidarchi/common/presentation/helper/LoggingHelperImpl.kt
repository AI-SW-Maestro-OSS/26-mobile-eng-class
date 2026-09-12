package com.jongchan.androidarchi.common.presentation.helper

import android.util.Log
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLogging
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingParam
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingRepository
import com.jongchan.androidarchi.common.domain.helper.LoggingHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Metric 로그를 fire-and-forget 으로 [MetricLoggingRepository] 에 넘긴다.
 *
 * - 호출 스레드를 막지 않고, 전송 실패가 앱 동작에 영향을 주지 않도록 모든 예외를 삼킨다(Logcat 경고만).
 * - [MetricLogging.availableParams] 에 없는 파라미터는 버리고 경고를 남긴다(시트 헤더와의 계약 유지).
 * - 전송 대상은 Google SpreadSheet `LoggingBucket` (탭 = event.name, 1행 헤더 = timestamp + param.name).
 *   `timestamp` 는 Apps Script 실행 시각으로 서버에서 채우므로 앱은 보내지 않는다.
 */
class LoggingHelperImpl(
    private val repository: MetricLoggingRepository,
    private val scope: CoroutineScope,
    private val isEnabled: Boolean,
    private val isDebug: Boolean,
) : LoggingHelper {

    override fun shotMetricLogging(
        event: MetricLogging,
        params: Map<out MetricLoggingParam, Any?>,
    ) {
        val filteredParams = filterParams(event, params)

        if (isDebug) {
            Log.d(TAG, "Shot Metric Logging: event=${event.name}, type=${event.type}, params=$filteredParams")
        }
        if (!isEnabled) {
            Log.w(TAG, "Metric logging is disabled (METRIC_LOG_URL is empty). Skip: ${event.name}")
            return
        }

        scope.launch {
            runCatching {
                repository.send(
                    event = event,
                    params = filteredParams,
                )
            }.onFailure {
                Log.w(TAG, "Failed to send metric logging: event=${event.name}, cause=${it.message}")
            }
        }
    }

    private fun filterParams(
        event: MetricLogging,
        params: Map<out MetricLoggingParam, Any?>,
    ): Map<MetricLoggingParam, Any?> {
        val (allowed, rejected) = params.entries.partition { it.key in event.availableParams }
        if (rejected.isNotEmpty()) {
            Log.w(
                TAG,
                "Dropped params not declared in ${event.name}.availableParams: ${rejected.map { it.key.name }}",
            )
        }
        return allowed.associate { it.key to it.value }
    }

    private companion object {
        const val TAG = "MetricLogging"
    }
}
