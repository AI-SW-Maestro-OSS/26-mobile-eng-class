import Foundation

public struct FavoriteItemVO: Equatable, Hashable, Sendable {
    public let type: MediaType
    public let title: String
    public let urlKey: String
    public let thumbnailUrl: String
    public let contentsImageUrl: String
    public let dateTime: String

    public init(
        type: MediaType,
        title: String,
        urlKey: String,
        thumbnailUrl: String = "",
        contentsImageUrl: String = "",
        dateTime: String = ""
    ) {
        self.type = type
        self.title = title
        self.urlKey = urlKey
        self.thumbnailUrl = thumbnailUrl
        self.contentsImageUrl = contentsImageUrl
        self.dateTime = dateTime
    }
}
