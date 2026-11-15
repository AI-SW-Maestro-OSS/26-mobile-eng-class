import Foundation

public protocol MetricLoggingApiService: Sendable {
    func send(
        url: String,
        body: MetricLogRequestDTO
    ) async throws -> HttpResponse<MetricLogResponseDTO>
}

public final class MetricLoggingApiServiceImpl: MetricLoggingApiService {
    private let retrofit: RetrofitClient

    public init(retrofit: RetrofitClient) {
        self.retrofit = retrofit
    }

    public func send(
        url: String,
        body: MetricLogRequestDTO
    ) async throws -> HttpResponse<MetricLogResponseDTO> {
        try await retrofit.post(url: url, body: body, responseType: MetricLogResponseDTO.self)
    }
}
