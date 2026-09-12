package com.jongchan.androidarchi.common.domain.analytics.metric

/**
 * Metric 로깅 활성 여부. 값의 출처(BuildConfig.METRIC_LOG_URL)는 data 레이어가 알고,
 * presentation 은 이 객체만 본다 — presentation 이 data 의 BuildConfig 를 참조하지 않게 하기 위한 seam.
 */
data class MetricLoggingConfig(
    val isEnabled: Boolean = false,
)
