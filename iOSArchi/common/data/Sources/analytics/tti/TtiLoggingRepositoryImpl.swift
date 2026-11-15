import CommonDomain
import Foundation

public final class TtiLoggingRepositoryImpl: TtiLoggingRepository {
    public static let TTI_REPORT_EVENT = "tti_report"

    private let dataSource: TtiLoggingDataSource
    private let token: String

    public init(dataSource: TtiLoggingDataSource, token: String) {
        self.dataSource = dataSource
        self.token = token
    }

    public func send(report: [String: Any?]) async throws -> Bool {
        var mappedParams: [String: JsonElement] = [:]
        for (name, value) in report {
            mappedParams[name] = JsonElement.fromAny(value)
        }
        let request = TtiLogRequestDTO(
            token: token,
            event: Self.TTI_REPORT_EVENT,
            params: mappedParams
        )
        let response = try await dataSource.send(request)
        if response.ok != true {
            throw NSError(
                domain: "TtiLogging",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "TTI logging rejected by server: \(response.error ?? "unknown")"]
            )
        }
        return true
    }
}
