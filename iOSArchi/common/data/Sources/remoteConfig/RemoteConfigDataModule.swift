import CommonDomain
import Foundation

public enum RemoteConfigDataModule: DIModule {
    public static func register(in container: DIContainer) {
        container.register(ApiEndpointProvider.self, scope: .singleton) { _ in
            provideApiEndpointProvider()
        }
        container.register(RemoteConfigRepository.self, scope: .singleton) { _ in
            provideRemoteConfigRepositoryV2()
        }
    }

    public static func provideApiEndpointProvider() -> ApiEndpointProvider {
        ApiEndpointProviderImpl()
    }

    public static func provideRemoteConfigRepositoryV2() -> RemoteConfigRepository {
        // FIREBASE-CONFIG-INJECTION-POINT: GoogleService-Info.plist 가 없으면 DefaultRemoteConfigRepository 로 대체해
        // 앱은 정상 실행되고 모든 키는 defaultValue 를 돌려준다.
        DefaultRemoteConfigRepository()
    }
}
