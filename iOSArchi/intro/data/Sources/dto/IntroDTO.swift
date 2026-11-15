import CommonEntity
import Foundation
import IntroEntity

public struct IntroResponseDTO: Codable, Sendable, Equatable {
    public let title: String?
    public let subtitle: String?
    public let version: String?

    public init(
        title: String? = nil,
        subtitle: String? = nil,
        version: String? = nil
    ) {
        self.title = title
        self.subtitle = subtitle
        self.version = version
    }

    public func toVO() -> IntroVO {
        IntroVO(
            title: title ?? UNKNOWN,
            subtitle: subtitle ?? UNKNOWN,
            version: version ?? "1.0.0"
        )
    }
}
