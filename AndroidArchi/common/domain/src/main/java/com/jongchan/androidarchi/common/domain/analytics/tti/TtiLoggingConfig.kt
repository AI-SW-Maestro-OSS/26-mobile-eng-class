package com.jongchan.androidarchi.common.domain.analytics.tti

/**
 * TTI 로깅 활성 여부. 값의 출처(BuildConfig.TTI_LOG_URL)
 */
data class TtiLoggingConfig(
    val isEnabled: Boolean = false,
)
