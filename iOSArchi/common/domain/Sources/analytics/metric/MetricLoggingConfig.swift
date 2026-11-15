import Foundation

/// Metric 로깅 활성 여부.
public struct MetricLoggingConfig: Equatable, Sendable {
    public let isEnabled: Bool

    public init(isEnabled: Bool = false) {
        self.isEnabled = isEnabled
    }
}
