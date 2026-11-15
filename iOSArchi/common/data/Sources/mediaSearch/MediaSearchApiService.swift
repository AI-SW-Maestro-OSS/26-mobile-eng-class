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
    static var SORT_RECENCY: String { "recency" }
    static var PAGE_SIZE: Int { 10 }

    func searchImages(query: String, page: Int) async throws -> HttpResponse<ImageSearchResponse> {
        try await searchImages(query: query, page: page, sort: Self.SORT_RECENCY, size: Self.PAGE_SIZE)
    }

    func searchVideos(query: String, page: Int) async throws -> HttpResponse<VideoSearchResponse> {
        try await searchVideos(query: query, page: page, sort: Self.SORT_RECENCY, size: Self.PAGE_SIZE)
    }
}

public final class MediaSearchApiServiceImpl: MediaSearchApiService {
    private let retrofit: RetrofitClient

    public init(retrofit: RetrofitClient) {
        self.retrofit = retrofit
    }

    public func searchImages(
        query: String,
        page: Int,
        sort: String,
        size: Int
    ) async throws -> HttpResponse<ImageSearchResponse> {
        try await retrofit.get(
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
        try await retrofit.get(
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
