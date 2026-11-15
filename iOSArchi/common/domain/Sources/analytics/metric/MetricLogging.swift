import Foundation

public enum MetricEventType: String, Sendable {
    case Track
    case Exposure
}

public struct MetricLogging: Equatable, Hashable, Sendable {
    public let name: String
    public let type: MetricEventType
    public let availableParams: Set<MetricLoggingParam>

    public init(
        name: String,
        type: MetricEventType,
        availableParams: Set<MetricLoggingParam>
    ) {
        self.name = name
        self.type = type
        self.availableParams = availableParams
    }

    public static let ClickFavoriteToggleSearchPage = MetricLogging(
        name: "click_favorite_toggle_search_page",
        type: .Track,
        availableParams: [
            .AllFavoriteItemCount,
            .ItemImgUrl,
            .SearchKeyword,
        ]
    )
}
