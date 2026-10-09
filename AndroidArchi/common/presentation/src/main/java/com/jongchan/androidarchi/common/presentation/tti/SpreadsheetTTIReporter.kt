package com.jongchan.androidarchi.common.presentation.tti

import android.util.Log
import com.jongchan.androidarchi.common.domain.analytics.tti.TtiLoggingRepository
import com.jongchan.androidarchi.tti.TTIReporter
import com.jongchan.androidarchi.tti.TTI_PREFIX
import com.jongchan.androidarchi.tti.TimelineCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * TTI 리포트를 `LoggingBucket` 시트의 `tti_report` 탭(Apps Script)으로 흘려보내는 [TTIReporter].
 *
 * - `stopView` 는 인스턴스당 정확히 1회(shot/타임아웃 선착) 호출되므로, 그대로 시트 1행이 된다.
 * - `:tti` 는 duration 을 나노초로 기록하므로 ms 로 변환해 보낸다. 미측정 sentinel(-1)은 그대로 둔다 —
 *   분포 분석 시 `is_bounced=FALSE` 행만 걸러 쓰는 계약의 근거.
 * - 전송은 별도 scope 의 fire-and-forget 이라 호출 스레드(TtiDispatcher 직렬 레인)를 막지 않고,
 *   발사 직후 TTI 인스턴스 scope 가 cancel 되어도(TTIHelperImpl.release) 전송은 살아남는다.
 * - 실패는 Logcat 경고만 — 계측이 앱 동작을 막거나 죽이면 안 된다.
 */
class SpreadsheetTTIReporter(
    private val repository: TtiLoggingRepository,
    private val scope: CoroutineScope,
    private val isEnabled: Boolean,
) : TTIReporter {

    override fun startView(key: String, name: String, attributes: Map<String, Any?>) = Unit

    override fun stopView(key: String, attributes: Map<String, Any?>) {
        if (!isEnabled) {
            Log.w(TAG, "TTI logging is disabled (TTI_LOG_URL is empty). Skip: $key")
            return
        }
        val report = attributes.entries.associate { (rawKey, value) ->
            val fieldName = rawKey.removePrefix(TTI_PREFIX)
            fieldName to if (fieldName in durationFields) value.nanosToMillis() else value
        }
        scope.launch {
            runCatching {
                repository.send(report)
            }.onFailure {
                Log.w(TAG, "Failed to send TTI logging: key=$key, cause=${it.message}")
            }
        }
    }

    // 계산된 duration 만 Long 이고, 미측정 초기값(-1)은 Int 로 들어오므로 변환 대상에서 자연히 제외된다.
    private fun Any?.nanosToMillis(): Any? =
        if (this is Long && this >= 0L) this / NANOS_PER_MILLI else this

    private companion object {
        const val TAG = "TTILogging"
        const val NANOS_PER_MILLI = 1_000_000L

        val durationFields: Set<String> = TimelineCategory.entries.map { it.categoryName }.toSet()
    }
}
