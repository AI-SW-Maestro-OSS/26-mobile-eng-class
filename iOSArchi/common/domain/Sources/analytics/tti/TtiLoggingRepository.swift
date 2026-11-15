import Foundation

/// TTI 리포트를 외부 저장소(`LoggingBucket` 시트의 `tti_report` 탭 → Apps Script Web App)로 보내는 추상화.
public protocol TtiLoggingRepository: Sendable {
    func send(report: [String: Any?]) async throws -> Bool
}
