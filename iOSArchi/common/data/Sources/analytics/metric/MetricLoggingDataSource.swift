import Foundation

public final class MetricLoggingDataSource: BaseRemoteDataSource, @unchecked Sendable {
    private let apiService: MetricLoggingApiService
    private let endpointUrl: String

    public init(apiService: MetricLoggingApiService, endpointUrl: String) {
        self.apiService = apiService
        self.endpointUrl = endpointUrl
        super.init()
    }

    public func send(_ request: MetricLogRequestDTO) async throws -> MetricLogResponseDTO {
        try checkResponse(await apiService.send(url: endpointUrl, body: request))
    }
}
