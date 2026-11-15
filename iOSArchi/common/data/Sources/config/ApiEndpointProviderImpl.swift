import CommonDomain
import Foundation

/// 빌드 타입으로 환경을 결정하는 기본 구현.
/// API-CONFIG-INJECTION-POINT: 개발자 모드에서 환경을 바꾸는 기능이 생기면 여기서 저장된 값을 읽도록 교체한다.
public final class ApiEndpointProviderImpl: ApiEndpointProvider {
    public init() {}

    public func getApiEndpoint() -> ApiEndpoint {
        BuildConfig.DEBUG ? .Dev : .Prod
    }
}
