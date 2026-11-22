import CommonDomain
import Foundation

public struct EtcPage: Page, Equatable {
    public static let path = "/etc"

    public init() {}

    public func toRoute() -> NavRoute {
        NavRoute(path: Self.path)
    }
}
