import CommonData
import Foundation

public protocol IntroApiService: Sendable {
    func getIntro() async throws -> HttpResponse<IntroResponseDTO>
}

public final class DefaultIntroApiService: IntroApiService, @unchecked Sendable {
    private let httpClient: HTTPClient

    public init(httpClient: HTTPClient) {
        self.httpClient = httpClient
    }

    public func getIntro() async throws -> HttpResponse<IntroResponseDTO> {
        let dto = IntroResponseDTO(
            title: "iOSArchi",
            subtitle: "Multi-Module Clean Architecture + MVI + SwiftUI",
            version: "1.0.0"
        )
        return HttpResponse(
            code: 200,
            message: "OK",
            body: dto,
            requestUrl: "https://example.architecture/intro"
        )
    }
}
