import CommonEntity
import Foundation

public protocol MediaSearchRepository: Sendable {
    /// 키워드로 이미지+동영상 검색 결과의 한 페이지를 가져온다.
    /// 페이지 내부에서만 datetime 최신순으로 정렬되며, 페이지 간 정렬은 하지 않는다.
    func search(query: String, page: Int) async throws -> MediaSearchResultVO
}
