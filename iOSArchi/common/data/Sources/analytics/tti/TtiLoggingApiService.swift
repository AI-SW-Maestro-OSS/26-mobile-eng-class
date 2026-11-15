import Foundation

public protocol TtiLoggingApiService: Sendable {
    func send(
        url: String,
        body: TtiLogRequestDTO
    ) async throws -> HttpResponse<TtiLogResponseDTO>
}

public final class TtiLoggingApiServiceImpl: TtiLoggingApiService {
    private let retrofit: RetrofitClient

    public init(retrofit: RetrofitClient) {
        self.retrofit = retrofit
    }

    public func send(
        url: String,
        body: TtiLogRequestDTO
    ) async throws -> HttpResponse<TtiLogResponseDTO> {
        try await retrofit.post(url: url, body: body, responseType: TtiLogResponseDTO.self)
    }
}
