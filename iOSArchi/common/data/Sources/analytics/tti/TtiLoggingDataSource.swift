import Foundation

public final class TtiLoggingDataSource: BaseRemoteDataSource, @unchecked Sendable {
    private let apiService: TtiLoggingApiService
    private let endpointUrl: String

    public init(apiService: TtiLoggingApiService, endpointUrl: String) {
        self.apiService = apiService
        self.endpointUrl = endpointUrl
        super.init()
    }

    public func send(_ request: TtiLogRequestDTO) async throws -> TtiLogResponseDTO {
        try checkResponse(await apiService.send(url: endpointUrl, body: request))
    }
}
