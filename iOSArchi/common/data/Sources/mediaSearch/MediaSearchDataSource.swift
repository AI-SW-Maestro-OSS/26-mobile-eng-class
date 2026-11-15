import Foundation

public final class MediaSearchDataSource: BaseRemoteDataSource, @unchecked Sendable {
    private let apiService: MediaSearchApiService

    public init(apiService: MediaSearchApiService) {
        self.apiService = apiService
        super.init()
    }

    public func searchImages(query: String, page: Int) async throws -> ImageSearchResponse {
        try checkResponse(await apiService.searchImages(query: query, page: page))
    }

    public func searchVideos(query: String, page: Int) async throws -> VideoSearchResponse {
        try checkResponse(await apiService.searchVideos(query: query, page: page))
    }
}
