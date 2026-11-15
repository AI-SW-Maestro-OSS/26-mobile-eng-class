import Foundation

/// 타입 안전한 Remote Config 조회.
/// 값을 얻지 못하면 항상 `RemoteConfigKey.defaultValue` 를 반환한다.
public protocol RemoteConfigRepository: Sendable {
    func get<T: Codable & Sendable>(_ key: RemoteConfigKey<T>) -> T
}
