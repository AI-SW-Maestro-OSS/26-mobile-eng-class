import Foundation

/// Remote Config 에서 JSON 으로 내려오는 툴팁 바텀시트 정보.
public struct BottomSheetVO: Codable, Equatable, Sendable {
    public let title: String
    public let description: String
    public let url: String

    public init(
        title: String = "",
        description: String = "",
        url: String = ""
    ) {
        self.title = title
        self.description = description
        self.url = url
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.title = try container.decodeIfPresent(String.self, forKey: .title) ?? ""
        self.description = try container.decodeIfPresent(String.self, forKey: .description) ?? ""
        self.url = try container.decodeIfPresent(String.self, forKey: .url) ?? ""
    }

    public static let empty = BottomSheetVO()
}
