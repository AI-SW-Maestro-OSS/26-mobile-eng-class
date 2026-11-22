import CommonEntity
import Foundation

public struct SearchMetaDTO: Codable, Equatable, Sendable {
    // true 면 현재 검색어로 더 불러올 페이지가 없음을 의미한다.
    public let isEnd: Bool?

    enum CodingKeys: String, CodingKey {
        case isEnd = "is_end"
    }

    public init(isEnd: Bool? = nil) {
        self.isEnd = isEnd
    }
}

public struct ImageSearchResponse: Codable, Equatable, Sendable {
    public let documents: [ImageDocumentDTO]?
    public let meta: SearchMetaDTO?

    public init(documents: [ImageDocumentDTO]? = nil, meta: SearchMetaDTO? = nil) {
        self.documents = documents
        self.meta = meta
    }
}

public struct ImageDocumentDTO: Codable, Equatable, Sendable {
    public let collection: String?
    public let thumbnailUrl: String?
    public let imageUrl: String?
    public let displaySitename: String?
    public let docUrl: String?
    public let datetime: String?

    enum CodingKeys: String, CodingKey {
        case collection
        case thumbnailUrl = "thumbnail_url"
        case imageUrl = "image_url"
        case displaySitename = "display_sitename"
        case docUrl = "doc_url"
        case datetime
    }

    public init(
        collection: String? = nil,
        thumbnailUrl: String? = nil,
        imageUrl: String? = nil,
        displaySitename: String? = nil,
        docUrl: String? = nil,
        datetime: String? = nil
    ) {
        self.collection = collection
        self.thumbnailUrl = thumbnailUrl
        self.imageUrl = imageUrl
        self.displaySitename = displaySitename
        self.docUrl = docUrl
        self.datetime = datetime
    }
}

public struct VideoSearchResponse: Codable, Equatable, Sendable {
    public let documents: [VideoDocumentDTO]?
    public let meta: SearchMetaDTO?

    public init(documents: [VideoDocumentDTO]? = nil, meta: SearchMetaDTO? = nil) {
        self.documents = documents
        self.meta = meta
    }
}

public struct VideoDocumentDTO: Codable, Equatable, Sendable {
    public let title: String?
    public let url: String?
    public let thumbnail: String?
    public let datetime: String?

    public init(
        title: String? = nil,
        url: String? = nil,
        thumbnail: String? = nil,
        datetime: String? = nil
    ) {
        self.title = title
        self.url = url
        self.thumbnail = thumbnail
        self.datetime = datetime
    }
}

public extension ImageDocumentDTO {
    // https://developers.kakao.com/docs/ko/daum-search/dev-guide#search-image
    func toVO() -> MediaItemVO {
        MediaItemVO(
            type: .image,
            title: displaySitename ?? UNKNOWN,
            urlKey: imageUrl ?? "",
            thumbnailImageUrl: thumbnailUrl ?? "",
            contentsImageUrl: imageUrl ?? "",
            pageLinkUrl: docUrl ?? "",
            collection: collection ?? "",
            dateTime: datetime ?? ""
        )
    }
}

public extension VideoDocumentDTO {
    // https://developers.kakao.com/docs/ko/daum-search/dev-guide#search-video
    func toVO() -> MediaItemVO {
        MediaItemVO(
            type: .video,
            title: title ?? UNKNOWN,
            urlKey: url ?? "",
            thumbnailImageUrl: thumbnail ?? "",
            contentsImageUrl: thumbnail ?? "",
            pageLinkUrl: url ?? "",
            collection: "",
            dateTime: datetime ?? ""
        )
    }
}
