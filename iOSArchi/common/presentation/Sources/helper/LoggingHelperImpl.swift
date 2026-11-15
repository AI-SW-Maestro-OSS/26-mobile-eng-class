import CommonDomain
import Foundation

public final class LoggingHelperImpl: LoggingHelper, @unchecked Sendable {
    private let repository: MetricLoggingRepository
    private let config: MetricLoggingConfig
    private let ioScope: ArchiCoroutineScope

    public init(
        repository: MetricLoggingRepository,
        config: MetricLoggingConfig,
        ioScope: ArchiCoroutineScope
    ) {
        self.repository = repository
        self.config = config
        self.ioScope = ioScope
    }

    public func shotMetricLogging(
        event: MetricLogging,
        params: [MetricLoggingParam: Any?]
    ) {
        let filteredParams = params.filter { event.availableParams.contains($0.key) }
        #if DEBUG
        print("[MetricLogging] event=\(event.name), type=\(event.type.rawValue), params=\(filteredParams)")
        #endif
        guard config.isEnabled else { return }
        let sendableBox = SendableParamsBox(params: filteredParams)
        ioScope.launch { [repository] in
            _ = try? await repository.send(event: event, params: sendableBox.params)
        }
    }
}

private struct SendableParamsBox: @unchecked Sendable {
    let params: [MetricLoggingParam: Any?]
}
