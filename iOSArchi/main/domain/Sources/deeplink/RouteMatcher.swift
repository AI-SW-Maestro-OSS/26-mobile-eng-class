import Foundation

public struct RouteMatchResult<T: Sendable>: Sendable {
    public let route: T
    public let pathParams: [String: String]

    public init(route: T, pathParams: [String: String]) {
        self.route = route
        self.pathParams = pathParams
    }
}

public enum RouteMatcher {
    public static func match<T: Sendable>(
        actualPath: String,
        candidates: [T],
        pathSelector: (T) -> String
    ) -> RouteMatchResult<T>? {
        var bestMatch: (route: T, params: [String: String], literalCount: Int)?

        for candidate in candidates {
            let pattern = RoutePattern(pathSelector(candidate))
            if let params = pattern.match(actualPath) {
                let literals = pattern.literalCount
                if let currentBest = bestMatch {
                    if literals > currentBest.literalCount {
                        bestMatch = (candidate, params, literals)
                    }
                } else {
                    bestMatch = (candidate, params, literals)
                }
            }
        }

        guard let winner = bestMatch else { return nil }
        return RouteMatchResult(route: winner.route, pathParams: winner.params)
    }
}
