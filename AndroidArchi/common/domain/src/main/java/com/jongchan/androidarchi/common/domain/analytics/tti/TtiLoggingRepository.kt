package com.jongchan.androidarchi.common.domain.analytics.tti

/**
 * TTI 리포트를 외부 저장소(현재는 `LoggingBucket` 시트의 `tti_report` 탭 → Apps Script Web App)로 보내는 추상화.
 *
 * TTI 는 화면별 소요시간 raw 표본을 인스턴스 단위로 쌓아 나중에 분포(퍼센타일 등)를 분석하는 성능 텔레메트리다.
 *
 * - [report] 의 key = 시트 `tti_report` 탭의 헤더 이름 (`page_name`, `tti_time`, ...), value 는 원시 타입.
 * - `timestamp` 는 앱이 아니라 Apps Script 가 실행 시각으로 기록한다.
 * - 호출부(SpreadsheetTTIReporter)가 예외를 처리하므로 여기서는 예외를 그대로 던져도 된다.
 *
 * @return 서버(Apps Script)가 append 성공을 응답했으면 true.
 */
interface TtiLoggingRepository {
    suspend fun send(report: Map<String, Any?>): Boolean
}
