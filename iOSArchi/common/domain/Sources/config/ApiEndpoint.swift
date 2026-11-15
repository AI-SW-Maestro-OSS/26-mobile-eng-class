import Foundation

/// 앱이 바라보는 서버 환경.
///
/// `environmentLevel` 이 높을수록 개발자 쪽 환경이다. Remote Config 는 키 뒤에 환경 이름을 붙여
/// (`spotEnableStage`, `spotEnablePublic` ...) 현재 환경 → 하위 환경 순으로 값을 탐색하므로,
/// 상위 환경에서만 값을 덮어쓰고 나머지는 Public 값을 공유할 수 있다.
public enum ApiEndpoint: String, CaseIterable, Sendable {
    case Dev
    case QA
    case Prod

    public var name: String { rawValue }

    public var environmentLevel: Int {
        switch self {
        case .Dev: return 3
        case .QA: return 2
        case .Prod: return 1
        }
    }

    /// 현재 환경을 포함해 그보다 낮은(=배포에 가까운) 환경을 높은 레벨부터 나열한다.
    public func fallbackChain() -> [ApiEndpoint] {
        Self.allCases
            .filter { $0.environmentLevel <= self.environmentLevel }
            .sorted { $0.environmentLevel > $1.environmentLevel }
    }
}

/// 현재 앱이 사용하는 `ApiEndpoint` 를 알려준다. 구현은 data 레이어에 둔다.
public protocol ApiEndpointProvider: Sendable {
    func getApiEndpoint() -> ApiEndpoint
}
