import CommonDomain
import Foundation

/// Firebase 가 초기화되지 않은 환경(GoogleService-Info.plist 미배치)에서 쓰는 대체 구현.
/// 모든 키에 대해 `RemoteConfigKey.defaultValue` 를 돌려주므로 호출부는 Firebase 유무를 몰라도 된다.
public final class DefaultRemoteConfigRepository: RemoteConfigRepository {
    private static let logCategory = "RemoteConfigV2"

    public init() {}

    public func get<T: Codable & Sendable>(_ key: RemoteConfigKey<T>) -> T {
        print("[\(Self.logCategory)] Firebase not initialized. Key \(key.key) uses default value: \(key.defaultValue)")
        return key.defaultValue
    }
}
