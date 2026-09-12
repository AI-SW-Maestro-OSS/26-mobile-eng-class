package com.jongchan.androidarchi.common.domain.analytics.metric

/**
 * Metric 로그를 외부 저장소(현재는 Google SpreadSheet `LoggingBucket` → Apps Script Web App)로 보내는 추상화.
 *
 * - 시트 탭 이름 = [MetricLogging.name], 1행 헤더 = `timestamp` + [MetricLoggingParam.name] 들.
 * - `timestamp` 는 앱이 아니라 Apps Script 가 실행 시각으로 기록한다(클라이언트 시계 불일치 방지).
 * - 구현은 data 레이어([com.jongchan.androidarchi.common.data.analytics.metric.MetricLoggingRepositoryImpl])에 둔다.
 * - 호출부([com.jongchan.androidarchi.common.domain.helper.LoggingHelper] 구현체)가 예외를 처리하므로
 *   여기서는 네트워크/파싱 예외를 그대로 던져도 된다.
 *
 * @return 서버(Apps Script)가 append 성공을 응답했으면 true.
 */
interface MetricLoggingRepository {
    suspend fun send(
        event: MetricLogging,
        params: Map<out MetricLoggingParam, Any?>,
    ): Boolean
}
