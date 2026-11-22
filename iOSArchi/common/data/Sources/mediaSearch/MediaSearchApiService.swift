import Foundation

public protocol MediaSearchApiService: Sendable {
    func searchImages(
        query: String,
        page: Int,
        sort: String,
        size: Int
    ) async throws -> HttpResponse<ImageSearchResponse>

    func searchVideos(
        query: String,
        page: Int,
        sort: String,
        size: Int
    ) async throws -> HttpResponse<VideoSearchResponse>
}

public extension MediaSearchApiService {
    static var sortRecency: String { "recency" }
    static var pageSize: Int { 10 }

    func searchImages(query: String, page: Int) async throws -> HttpResponse<ImageSearchResponse> {
        try await searchImages(query: query, page: page, sort: Self.sortRecency, size: Self.pageSize)
    }

    func searchVideos(query: String, page: Int) async throws -> HttpResponse<VideoSearchResponse> {
        try await searchVideos(query: query, page: page, sort: Self.sortRecency, size: Self.pageSize)
    }
}

public final class MediaSearchApiServiceImpl: MediaSearchApiService {
    private let httpClient: HTTPClient

    public init(httpClient: HTTPClient) {
        self.httpClient = httpClient
    }

    public func searchImages(
        query: String,
        page: Int,
        sort: String,
        size: Int
    ) async throws -> HttpResponse<ImageSearchResponse> {
        try await httpClient.get(
            path: "v2/search/image",
            query: [
                "query": query,
                "page": String(page),
                "sort": sort,
                "size": String(size),
            ],
            responseType: ImageSearchResponse.self
        )
    }

    public func searchVideos(
        query: String,
        page: Int,
        sort: String,
        size: Int
    ) async throws -> HttpResponse<VideoSearchResponse> {
        try await httpClient.get(
            path: "v2/search/vclip",
            query: [
                "query": query,
                "page": String(page),
                "sort": sort,
                "size": String(size),
            ],
            responseType: VideoSearchResponse.self
        )
    }
}
