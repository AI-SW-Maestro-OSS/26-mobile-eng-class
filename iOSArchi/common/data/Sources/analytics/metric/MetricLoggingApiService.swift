import Foundation

public protocol MetricLoggingApiService: Sendable {
    func send(
        url: String,
        body: MetricLogRequestDTO
    ) async throws -> HttpResponse<MetricLogResponseDTO>
}

public final class MetricLoggingApiServiceImpl: MetricLoggingApiService {
    private let httpClient: HTTPClient

    public init(httpClient: HTTPClient) {
        self.httpClient = httpClient
    }

    public func send(
        url: String,
        body: MetricLogRequestDTO
    ) async throws -> HttpResponse<MetricLogResponseDTO> {
        try await httpClient.post(url: url, body: body, responseType: MetricLogResponseDTO.self)
    }
}
