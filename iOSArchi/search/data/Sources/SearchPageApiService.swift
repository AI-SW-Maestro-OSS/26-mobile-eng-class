import CommonData
import Foundation

public protocol SearchPageApiService: Sendable {
    func getSearchPage() async throws -> HttpResponse<SearchPageResponseDTO>
}

public final class DefaultSearchPageApiService: SearchPageApiService, @unchecked Sendable {
    private let json: ArchiJson

    public init(json: ArchiJson = ArchiJson()) {
        self.json = json
    }

    public func getSearchPage() async throws -> HttpResponse<SearchPageResponseDTO> {
        HttpResponse(
            code: 200,
            message: "OK",
            body: SearchPageMockData.makeResponse(json: json),
            requestUrl: "https://example.architecture/sdui/search"
        )
    }
}
