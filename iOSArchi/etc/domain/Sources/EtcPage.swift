import CommonDomain
import Foundation

public struct EtcPage: Page, Equatable {
    public static let PATH = "/etc"

    public init() {}

    public func toRoute() -> NavRoute {
        NavRoute(path: Self.PATH)
    }
}
