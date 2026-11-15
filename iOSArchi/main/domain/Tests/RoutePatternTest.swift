import MainDomain
import XCTest

final class RoutePatternTest: XCTestCase {
    func testExactLiteralMatch() {
        let pattern = RoutePattern("/search")
        XCTAssertEqual(pattern.match("/search"), [:])
        XCTAssertEqual(pattern.match("search"), [:])
        XCTAssertNil(pattern.match("/favorite"))
        XCTAssertNil(pattern.match("/search/extra"))
    }

    func testParameterizedPathExtraction() {
        let pattern = RoutePattern("/users/{userId}/posts/{postId}")
        let params = pattern.match("/users/42/posts/99")
        XCTAssertEqual(params, ["userId": "42", "postId": "99"])
    }

    func testMismatchedSegmentCountReturnsNil() {
        let pattern = RoutePattern("/users/{userId}/posts/{postId}")
        XCTAssertNil(pattern.match("/users/42"))
        XCTAssertNil(pattern.match("/users/42/comments/99"))
    }
}
