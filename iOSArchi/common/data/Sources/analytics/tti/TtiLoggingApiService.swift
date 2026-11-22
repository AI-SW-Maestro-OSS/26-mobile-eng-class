import Foundation

public protocol TtiLoggingApiService: Sendable {
    func send(
        url: String,
        body: TtiLogRequestDTO
    ) async throws -> HttpResponse<TtiLogResponseDTO>
}

public final class TtiLoggingApiServiceImpl: TtiLoggingApiService {
    private let httpClient: HTTPClient

    public init(httpClient: HTTPClient) {
        self.httpClient = httpClient
    }

    public func send(
        url: String,
        body: TtiLogRequestDTO
    ) async throws -> HttpResponse<TtiLogResponseDTO> {
        try await httpClient.post(url: url, body: body, responseType: TtiLogResponseDTO.self)
    }
}
