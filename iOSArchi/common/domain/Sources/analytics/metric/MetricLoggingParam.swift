import Foundation

public struct MetricLoggingParam: Equatable, Hashable, Sendable {
    public let name: String

    public init(name: String) {
        self.name = name
    }

    public static let AllFavoriteItemCount = MetricLoggingParam(name: "all_favorite_item_count")
    public static let ItemImgUrl = MetricLoggingParam(name: "item_img_url")
    public static let SearchKeyword = MetricLoggingParam(name: "search_keyword")
}
