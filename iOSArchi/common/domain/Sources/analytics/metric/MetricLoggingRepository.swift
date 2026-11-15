import Foundation

/// Metric 로그를 외부 저장소(현재는 Google SpreadSheet `LoggingBucket` → Apps Script Web App)로 보내는 추상화.
public protocol MetricLoggingRepository: Sendable {
    func send(
        event: MetricLogging,
        params: [MetricLoggingParam: Any?]
    ) async throws -> Bool
}
