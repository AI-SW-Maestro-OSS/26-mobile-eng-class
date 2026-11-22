import CommonDomain
import CommonEntity
import Foundation

public final class MediaSearchRepositoryImpl: MediaSearchRepository {
    private static let SORT_KEY_LENGTH = 19
    private let searchDataSource: MediaSearchDataSource

    public init(searchDataSource: MediaSearchDataSource) {
        self.searchDataSource = searchDataSource
    }

    public func search(query: String, page: Int) async throws -> MediaSearchResultVO {
        // 이미지/동영상 검색을 동시에 호출한 뒤, 해당 페이지 안에서만 datetime 최신순으로 합쳐 정렬한다.
        async let imagesTask = searchDataSource.searchImages(query: query, page: page)
        async let videosTask = searchDataSource.searchVideos(query: query, page: page)

        let imageResponse = try await imagesTask
        let videoResponse = try await videosTask

        let rawList = (imageResponse.documents ?? []).map { $0.toVO() }
            + (videoResponse.documents ?? []).map { $0.toVO() }

        // 동일 식별자(이미지=image_url) 중복 항목을 제거해 리스트/페이저 key 충돌을 방지한다.
        var seen = Set<String>()
        var deduplicated: [MediaItemVO] = []
        for item in rawList {
            if seen.insert(item.urlKey).inserted {
                deduplicated.append(item)
            }
        }

        let merged = deduplicated
            .sorted { lhs, rhs in
                String(lhs.dateTime.prefix(Self.SORT_KEY_LENGTH)) > String(rhs.dateTime.prefix(Self.SORT_KEY_LENGTH))
            }
            .map { item in
                var updated = item
                updated.page = page
                return updated
            }

        return MediaSearchResultVO(
            items: merged,
            isEnd: (imageResponse.meta?.isEnd ?? true) && (videoResponse.meta?.isEnd ?? true)
        )
    }
}
