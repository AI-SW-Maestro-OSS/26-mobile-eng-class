import Foundation

/// 단일 통합 네비게이션 단위.
/// - `path`: 이동 대상 페이지의 식별자. 앱 내 이동과 deep-link 양쪽에서 같은 값을 사용한다. 예: "/search", "/fullScreenMedia"
/// - `args`: 페이지가 요구하는 인자. 모두 String 으로 직렬화된 형태로 전달되며, 복잡 타입은 JSON 문자열로 인코딩한다.
public struct NavRoute: Codable, Equatable, Hashable, Sendable {
    public let path: String
    public let args: [String: String]

    public init(path: String, args: [String: String] = [:]) {
        self.path = path
        self.args = args
    }
}

/// 각 페이지별로 정의되는 typed argument 의 마커.
public protocol Page: Sendable {
    func toRoute() -> NavRoute
}

/// NavRoute.args 인코딩/디코딩에 공용으로 사용하는 JSON 코덱.
public enum NavRouteJson {
    public static let decoder = JSONDecoder()
    public static let encoder = JSONEncoder()
}
