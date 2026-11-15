import CommonData
import CommonDomain
import Foundation
import SearchDomain

public struct SearchPageResponseDTO: Codable, Sendable, Equatable {
    public let components: [SDUIComponentDTO]?

    public init(components: [SDUIComponentDTO]? = nil) {
        self.components = components
    }

    public func toVO(_ json: ArchiJson = ArchiJson()) -> SearchPageVO {
        SearchPageVO(
            components: components.toSDUIComponents(json)
        )
    }
}
