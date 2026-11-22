import Foundation

public enum FavoriteErrorType: CaseIterable, HTTPErrorType {
    case unknown

    public var type: String {
        switch self {
        case .unknown: return "api.favorite.unknown"
        }
    }

    public var errorMsg: String {
        switch self {
        case .unknown: return "알수없는 에러가 발생했습니다."
        }
    }

    public var isHandledOnDomain: Bool {
        switch self {
        case .unknown: return true
        }
    }
}
