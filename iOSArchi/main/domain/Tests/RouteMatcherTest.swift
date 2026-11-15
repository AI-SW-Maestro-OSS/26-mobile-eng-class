import MainDomain
import XCTest

final class RouteMatcherTest: XCTestCase {
    private struct DummyRoute: Sendable, Equatable {
        let id: String
        let path: String
    }

    func testPrefersMoreSpecificLiteralRouteOverWildcard() {
        let candidates = [
            DummyRoute(id: "param", path: "/items/{id}"),
            DummyRoute(id: "featured", path: "/items/featured")
        ]

        let featuredMatch = RouteMatcher.match(
            actualPath: "/items/featured",
            candidates: candidates,
            pathSelector: \.path
        )
        XCTAssertEqual(featuredMatch?.route.id, "featured")
        XCTAssertEqual(featuredMatch?.pathParams, [:])

        let paramMatch = RouteMatcher.match(
            actualPath: "/items/123",
            candidates: candidates,
            pathSelector: \.path
        )
        XCTAssertEqual(paramMatch?.route.id, "param")
        XCTAssertEqual(paramMatch?.pathParams, ["id": "123"])
    }
}
