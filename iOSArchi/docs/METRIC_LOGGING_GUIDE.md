# Metric & TTI Logging Guide (`iOSArchi`)

## 1. 개요

`iOSArchi`는 Google Apps Script Web App 엔드포인트를 통해 Google SpreadSheet로 두 종류의 텔레메트리를 전송한다:
1. **Metric Logging**: 사용자 이벤트(`MetricLogging.ClickFavoriteToggleSearchPage` 등) → `LoggingHelper.shotMetricLogging(event:params:)` → `MetricLoggingRepository`.
2. **TTI Logging**: 화면 진입 완료 시점의 6단계 타임라인 측정값 → `SpreadsheetTTIReporter` → `TtiLoggingRepository`.

## 2. 환경 변수 / Info.plist 설정

`BuildConfig.swift`는 프로세스 환경 변수 또는 `Info.plist`에서 다음 키를 읽는다:
- `METRIC_LOG_URL`, `METRIC_LOG_TOKEN`
- `TTI_LOG_URL`, `TTI_LOG_TOKEN`
- 값이 비어 있으면 네트워크 호출을 생략(no-op)하고 디버그 콘솔 로그만 출력한다.
