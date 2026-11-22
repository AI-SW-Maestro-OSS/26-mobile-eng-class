import CommonDomain
import Foundation

public struct FavoritePage: Page, Equatable {
    public static let path = "/favorite"

    public init() {}

    public func toRoute() -> NavRoute {
        NavRoute(path: Self.path)
    }
}
