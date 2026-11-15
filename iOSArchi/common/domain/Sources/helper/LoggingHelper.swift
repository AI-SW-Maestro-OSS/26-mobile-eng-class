import Foundation

public protocol LoggingHelper: Sendable {
    func shotMetricLogging(
        event: MetricLogging,
        params: [MetricLoggingParam: Any?]
    )
}

public extension LoggingHelper {
    func shotMetricLogging(event: MetricLogging) {
        shotMetricLogging(event: event, params: [:])
    }
}
