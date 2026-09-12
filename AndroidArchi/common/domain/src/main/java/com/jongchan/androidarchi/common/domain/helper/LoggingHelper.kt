package com.jongchan.androidarchi.common.domain.helper

import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLogging
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingParam

interface LoggingHelper {
    fun shotMetricLogging(
        event: MetricLogging,
        params: Map<out MetricLoggingParam, Any?> = emptyMap(),
    )
}