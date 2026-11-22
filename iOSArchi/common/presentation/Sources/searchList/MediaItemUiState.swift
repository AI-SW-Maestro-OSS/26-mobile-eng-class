import CommonEntity
import Foundation

public struct MediaItemUiState: Sendable, Equatable, Identifiable {
    public var id: String { urlKey }

    public let type: MediaType
    public let title: String
    public let urlKey: String
    public let thumbnailUrl: String
    public let mediaUrl: String
    public let source: String
    public let dateTime: String
    public let page: Int
    public var isFavorite: Bool

    public var mediaType: MediaType { type }

    public init(
        type: MediaType = .image,
        title: String,
        urlKey: String,
        thumbnailUrl: String,
        mediaUrl: String,
        source: String = "",
        dateTime: String = "",
        page: Int = 1,
        isFavorite: Bool = false
    ) {
        self.type = type
        self.title = title
        self.urlKey = urlKey
        self.thumbnailUrl = thumbnailUrl
        self.mediaUrl = mediaUrl
        self.source = source
        self.dateTime = dateTime
        self.page = page
        self.isFavorite = isFavorite
    }

    public init(from vo: MediaItemVO, isFavorite: Bool) {
        self.init(
            type: vo.type,
            title: vo.title,
            urlKey: vo.urlKey,
            thumbnailUrl: vo.thumbnailImageUrl,
            mediaUrl: vo.contentsImageUrl,
            source: vo.collection.isEmpty ? vo.pageLinkUrl : vo.collection,
            dateTime: vo.dateTime,
            page: vo.page,
            isFavorite: isFavorite
        )
    }

    public init(from vo: FavoriteItemVO) {
        self.init(
            type: vo.type,
            title: vo.title,
            urlKey: vo.urlKey,
            thumbnailUrl: vo.thumbnailUrl,
            mediaUrl: vo.contentsImageUrl,
            source: "",
            dateTime: vo.dateTime,
            page: 1,
            isFavorite: true
        )
    }

    public func toFavoriteItemVO() -> FavoriteItemVO {
        FavoriteItemVO(
            type: type,
            title: title,
            urlKey: urlKey,
            thumbnailUrl: thumbnailUrl,
            contentsImageUrl: mediaUrl,
            dateTime: dateTime
        )
    }
}
