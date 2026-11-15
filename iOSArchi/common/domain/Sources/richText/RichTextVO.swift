import Foundation

public struct RichTextVO: Codable, Equatable, Sendable {
    public let text: String
    public let typeScale: String
    public let color: String
    public let link: String?

    public init(
        text: String,
        typeScale: String,
        color: String,
        link: String? = nil
    ) {
        self.text = text
        self.typeScale = typeScale
        self.color = color
        self.link = link
    }

    public var hasLink: Bool { link != nil }

    public static let empty: [RichTextVO] = []
}

public typealias RichText = [RichTextVO]
