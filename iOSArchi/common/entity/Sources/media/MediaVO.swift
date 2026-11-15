import Foundation

/// 이미지/동영상 검색 결과 1건.
///
/// 카카오 다음 검색 API 의 이미지/동영상 응답을 단일 모델로 합쳐 표현한다.
/// - `title`      : display_sitename(이미지) / title(동영상)
/// - `urlKey`     : 항목 고유 식별 키(이미지 표시용이 아님). image_url(이미지) / url(동영상).
/// - `pageLinkUrl`    : doc_url(이미지) / url(동영상). 리스트에 표시하는 주소 겸 문서 링크.
/// - `thumbnailImageUrl`: 썸네일(목록/그리드 표시). thumbnail_url(이미지) / thumbnail(동영상)
/// - `contentsImageUrl` : 풀스크린에 띄울 원본 이미지. image_url(이미지) / thumbnail(동영상, 원본이 없어 썸네일 사용)
/// - `collection` : 이미지 전용 카테고리. 동영상은 빈 문자열.
/// - `dateTime`   : datetime(ISO 8601 원본 문자열)
/// - `page`       : 이 항목이 속한 검색 페이지(1-based). 페이지 구분자 렌더링에 사용.
public struct MediaItemVO: Codable, Equatable, Hashable, Sendable {
    public let type: MediaType
    public let title: String
    public let urlKey: String
    public let thumbnailImageUrl: String
    public let contentsImageUrl: String
    public let pageLinkUrl: String
    public let collection: String
    public let dateTime: String
    public var page: Int

    public init(
        type: MediaType,
        title: String,
        urlKey: String,
        thumbnailImageUrl: String = "",
        contentsImageUrl: String = "",
        pageLinkUrl: String = "",
        collection: String = "",
        dateTime: String = "",
        page: Int = 1
    ) {
        self.type = type
        self.title = title
        self.urlKey = urlKey
        self.thumbnailImageUrl = thumbnailImageUrl
        self.contentsImageUrl = contentsImageUrl
        self.pageLinkUrl = pageLinkUrl
        self.collection = collection
        self.dateTime = dateTime
        self.page = page
    }

    public static let empty = MediaItemVO(type: .UNKNOWN, title: "", urlKey: "")

    public var isEmpty: Bool { self == Self.empty }

    public func copy(page: Int? = nil) -> MediaItemVO {
        MediaItemVO(
            type: type,
            title: title,
            urlKey: urlKey,
            thumbnailImageUrl: thumbnailImageUrl,
            contentsImageUrl: contentsImageUrl,
            pageLinkUrl: pageLinkUrl,
            collection: collection,
            dateTime: dateTime,
            page: page ?? self.page
        )
    }
}

/// 검색 결과 한 페이지.
/// - `items` : 이미지+동영상을 합쳐 datetime 최신순으로 정렬한 항목들.
/// - `isEnd` : 더 불러올 페이지가 없으면 true.
public struct MediaSearchResultVO: Equatable, Sendable {
    public let items: [MediaItemVO]
    public let isEnd: Bool

    public init(items: [MediaItemVO], isEnd: Bool) {
        self.items = items
        self.isEnd = isEnd
    }
}

public enum MediaType: String, Codable, CaseIterable, Sendable {
    case UNKNOWN
    case IMAGE
    case VIDEO

    public var rawValueInt: Int {
        switch self {
        case .UNKNOWN: return 0
        case .IMAGE: return 1
        case .VIDEO: return 3
        }
    }

    public var name: String { rawValue }

    public static func fromRawValue(_ value: Int?) -> MediaType {
        guard let value else { return .UNKNOWN }
        return Self.allCases.first { $0.rawValueInt == value } ?? .UNKNOWN
    }

    public static func valueOf(_ name: String) -> MediaType? {
        MediaType(rawValue: name)
    }
}
