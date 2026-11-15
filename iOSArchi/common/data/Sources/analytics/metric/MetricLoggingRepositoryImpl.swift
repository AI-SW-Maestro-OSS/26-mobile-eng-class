import CommonDomain
import Foundation

public final class MetricLoggingRepositoryImpl: MetricLoggingRepository {
    private let dataSource: MetricLoggingDataSource
    private let token: String

    public init(dataSource: MetricLoggingDataSource, token: String) {
        self.dataSource = dataSource
        self.token = token
    }

    public func send(
        event: MetricLogging,
        params: [MetricLoggingParam: Any?]
    ) async throws -> Bool {
        var mappedParams: [String: JsonElement] = [:]
        for (param, value) in params {
            mappedParams[param.name] = JsonElement.fromAny(value)
        }
        let request = MetricLogRequestDTO(
            token: token,
            event: event.name,
            params: mappedParams
        )
        let response = try await dataSource.send(request)
        if response.ok != true {
            throw NSError(
                domain: "MetricLogging",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "Metric logging rejected by server: \(response.error ?? "unknown")"]
            )
        }
        return true
    }
}
