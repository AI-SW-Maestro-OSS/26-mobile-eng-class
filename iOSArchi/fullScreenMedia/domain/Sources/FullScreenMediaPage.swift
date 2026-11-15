import CommonDomain
import Foundation

public struct FullScreenMediaPage: Page, Equatable {
    public static let PATH = "/fullScreenMedia"

    public struct Args: Codable, Sendable, Equatable {
        public let origin: FullScreenMediaOrigin
        public let initialIndex: Int
        public let query: String

        public init(
            origin: FullScreenMediaOrigin = .SEARCH,
            initialIndex: Int = 0,
            query: String = ""
        ) {
            self.origin = origin
            self.initialIndex = initialIndex
            self.query = query
        }
    }

    public let origin: FullScreenMediaOrigin
    public let initialIndex: Int
    public let query: String

    public init(
        origin: FullScreenMediaOrigin = .SEARCH,
        initialIndex: Int = 0,
        query: String = ""
    ) {
        self.origin = origin
        self.initialIndex = initialIndex
        self.query = query
    }

    public init(args: Args) {
        self.origin = args.origin
        self.initialIndex = args.initialIndex
        self.query = args.query
    }

    public var args: Args {
        Args(origin: origin, initialIndex: initialIndex, query: query)
    }

    public func toRoute() -> NavRoute {
        NavRoute(
            path: Self.PATH,
            args: [
                "origin": origin.rawValue,
                "initialIndex": String(initialIndex),
                "query": query
            ]
        )
    }
}
