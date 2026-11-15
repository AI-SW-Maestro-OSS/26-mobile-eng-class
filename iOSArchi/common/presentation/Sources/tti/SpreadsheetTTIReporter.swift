import CommonDomain
import Foundation
import TTI

public final class SpreadsheetTTIReporter: TTIReporter, @unchecked Sendable {
    private let repository: TtiLoggingRepository
    private let config: TtiLoggingConfig
    private let scope: ArchiCoroutineScope

    public init(
        repository: TtiLoggingRepository,
        config: TtiLoggingConfig,
        scope: ArchiCoroutineScope
    ) {
        self.repository = repository
        self.config = config
        self.scope = scope
    }

    public func startView(key: String, name: String, attributes: [String: Any]) {}

    public func stopView(key: String, attributes: [String: Any]) {
        guard config.isEnabled else { return }
        var payload: [String: Any?] = ["tti.key": key]
        for (k, v) in attributes {
            payload[k] = v
        }
        let box = SendableReportBox(report: payload)
        scope.launch { [repository] in
            _ = try? await repository.send(report: box.report)
        }
    }
}

private struct SendableReportBox: @unchecked Sendable {
    let report: [String: Any?]
}
