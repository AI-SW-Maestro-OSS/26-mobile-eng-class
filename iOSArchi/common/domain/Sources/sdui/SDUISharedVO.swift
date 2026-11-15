import Foundation

/// "image" : { "imgUrl", "size", "bgColor" }
public struct SDUIImageVO: Codable, Equatable, Hashable, Sendable {
    public let imgUrl: String
    public let size: SDUISizeVO
    public let bgColor: String

    public init(imgUrl: String, size: SDUISizeVO, bgColor: String) {
        self.imgUrl = imgUrl
        self.size = size
        self.bgColor = bgColor
    }
}

/// "size" : { "width", "height" }
public struct SDUISizeVO: Codable, Equatable, Hashable, Sendable {
    public let width: Int
    public let height: Int

    public init(width: Int, height: Int) {
        self.width = width
        self.height = height
    }
}

/// "actionIcon" : { "imgUrl", "size", "action" }
public struct SDUIActionIconVO: Codable, Equatable, Hashable, Sendable {
    public let imgUrl: String
    public let size: SDUISizeVO
    public let action: SDUIActionVO

    public init(imgUrl: String, size: SDUISizeVO, action: SDUIActionVO) {
        self.imgUrl = imgUrl
        self.size = size
        self.action = action
    }
}

/// "action" : { "navigateLink", "actionType" }
public struct SDUIActionVO: Codable, Equatable, Hashable, Sendable {
    public let navigateLink: String
    public let actionType: String

    public init(navigateLink: String = "", actionType: String = "") {
        self.navigateLink = navigateLink
        self.actionType = actionType
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.navigateLink = try container.decodeIfPresent(String.self, forKey: .navigateLink) ?? ""
        self.actionType = try container.decodeIfPresent(String.self, forKey: .actionType) ?? ""
    }
}
