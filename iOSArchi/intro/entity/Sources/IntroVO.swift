import CommonEntity
import Foundation

public struct IntroVO: Sendable, Equatable {
    public let title: String
    public let subtitle: String
    public let version: String

    public init(
        title: String = UNKNOWN,
        subtitle: String = UNKNOWN,
        version: String = "1.0.0"
    ) {
        self.title = title
        self.subtitle = subtitle
        self.version = version
    }
}
