import CommonDomain
import Foundation

/// Remote Config 키-값 프로바이더 추상화 (Firebase RemoteConfig 또는 인메모리 스토어 연동).
public protocol RemoteConfigDataStore: Sendable {
    func getKeysByPrefix(_ prefix: String) -> Set<String>
    func getString(_ key: String) -> String?
    func getBool(_ key: String) -> Bool?
    func getInt(_ key: String) -> Int?
    func getDouble(_ key: String) -> Double?
}

/// Remote Config 기반 `RemoteConfigRepository` 구현.
/// 조회는 `key + 환경이름` 으로 합성한 raw key 를 현재 환경 → 하위 환경 순으로 탐색한다.
public final class RemoteConfigRepositoryImpl: RemoteConfigRepository {
    private static let logCategory = "RemoteConfigV2"

    private let dataStore: RemoteConfigDataStore
    private let apiEndpointProvider: ApiEndpointProvider
    private let json: ArchiJson

    public init(
        dataStore: RemoteConfigDataStore,
        apiEndpointProvider: ApiEndpointProvider,
        json: ArchiJson
    ) {
        self.dataStore = dataStore
        self.apiEndpointProvider = apiEndpointProvider
        self.json = json
    }

    private var endpoint: ApiEndpoint {
        apiEndpointProvider.getApiEndpoint()
    }

    public func get<T: Codable & Sendable>(_ key: RemoteConfigKey<T>) -> T {
        for target in endpoint.fallbackChain() {
            let rawKey = key.key + target.name
            guard dataStore.getKeysByPrefix(key.key).contains(rawKey) else {
                continue
            }

            if T.self == Bool.self, let val = dataStore.getBool(rawKey) as? T {
                return val
            }
            if T.self == Int.self, let val = dataStore.getInt(rawKey) as? T {
                return val
            }
            if T.self == Double.self, let val = dataStore.getDouble(rawKey) as? T {
                return val
            }
            if T.self == String.self, let val = dataStore.getString(rawKey) as? T {
                return val
            }
            if let rawString = dataStore.getString(rawKey),
               let decoded = try? json.decodeFromString(T.self, rawString) {
                return decoded
            }
        }
        return key.defaultValue
    }
}
