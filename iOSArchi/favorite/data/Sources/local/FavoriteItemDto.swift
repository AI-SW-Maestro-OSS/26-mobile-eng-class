import CommonEntity
import Foundation

public struct FavoriteItemDto: Codable, Sendable, Equatable {
    public let type: String?
    public let title: String?
    public let urlKey: String?
    public let thumbnailUrl: String?
    public let contentsImageUrl: String?
    public let dateTime: String?

    public init(
        type: String? = nil,
        title: String? = nil,
        urlKey: String? = nil,
        thumbnailUrl: String? = nil,
        contentsImageUrl: String? = nil,
        dateTime: String? = nil
    ) {
        self.type = type
        self.title = title
        self.urlKey = urlKey
        self.thumbnailUrl = thumbnailUrl
        self.contentsImageUrl = contentsImageUrl
        self.dateTime = dateTime
    }

    public init(from vo: FavoriteItemVO) {
        self.init(
            type: vo.type.rawValue,
            title: vo.title,
            urlKey: vo.urlKey,
            thumbnailUrl: vo.thumbnailUrl,
            contentsImageUrl: vo.contentsImageUrl,
            dateTime: vo.dateTime
        )
    }

    public func toVO() -> FavoriteItemVO {
        FavoriteItemVO(
            type: MediaType.valueOf(type ?? "IMAGE") ?? .IMAGE,
            title: title ?? UNKNOWN,
            urlKey: urlKey ?? UNKNOWN,
            thumbnailUrl: thumbnailUrl ?? "",
            contentsImageUrl: contentsImageUrl ?? "",
            dateTime: dateTime ?? ""
        )
    }
}
