import CommonDomain
import Foundation

public struct IntroPage: Page, Equatable {
    public static let PATH = "/intro"

    public init() {}

    public func toRoute() -> NavRoute {
        NavRoute(path: Self.PATH)
    }
}
