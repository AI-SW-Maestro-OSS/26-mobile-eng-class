import CommonDomain
import Foundation

public struct SearchPage: Page, Equatable {
    public static let path = "/search"

    public struct Args: Codable, Sendable, Equatable {
        public let query: String?

        public init(query: String? = nil) {
            self.query = query
        }
    }

    public let query: String?

    public init(query: String? = nil) {
        self.query = query
    }

    public init(args: Args) {
        self.query = args.query
    }

    public func toRoute() -> NavRoute {
        var routeArgs: [String: String] = [:]
        if let query, !query.isEmpty {
            routeArgs["query"] = query
        }
        return NavRoute(path: Self.path, args: routeArgs)
    }
}
