import CommonDomain
import Foundation

public struct SearchPageVO: Sendable, Equatable {
    public let components: [SDUIViewTypeVO]

    public init(components: [SDUIViewTypeVO] = []) {
        self.components = components
    }
}
