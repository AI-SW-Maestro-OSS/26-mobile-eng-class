import Foundation

/// 디버그 빌드에서 Remote Config 로 내려받는 내장 테스트 계정.
public struct TestAccountVO: Codable, Equatable, Sendable {
    public let id: String
    public let pwd: String

    public init(id: String = "", pwd: String = "") {
        self.id = id
        self.pwd = pwd
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.id = try container.decodeIfPresent(String.self, forKey: .id) ?? ""
        self.pwd = try container.decodeIfPresent(String.self, forKey: .pwd) ?? ""
    }

    public static let empty = TestAccountVO()
}
