import Foundation

public enum FavoriteErrorType: CaseIterable, HttpErrorType {
    case UNKNOWN

    public var type: String {
        switch self {
        case .UNKNOWN: return "api.favorite.unknown"
        }
    }

    public var errorMsg: String {
        switch self {
        case .UNKNOWN: return "알수없는 에러가 발생했습니다."
        }
    }

    public var isHandledOnDomain: Bool {
        switch self {
        case .UNKNOWN: return true
        }
    }
}
