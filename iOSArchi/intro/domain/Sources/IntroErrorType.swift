import CommonDomain
import Foundation

public enum IntroErrorType: CaseIterable, HttpErrorType {
    case INVALID_TOKEN
    case MAINTENANCE
    case FORCE_UPDATE_REQUIRED

    public var type: String {
        switch self {
        case .INVALID_TOKEN: return "api.intro.invalid_token"
        case .MAINTENANCE: return "api.intro.maintenance"
        case .FORCE_UPDATE_REQUIRED: return "api.intro.force_update_required"
        }
    }

    public var errorMsg: String {
        switch self {
        case .INVALID_TOKEN: return "Authentication failed. Please sign in again."
        case .MAINTENANCE: return "Server is currently under maintenance."
        case .FORCE_UPDATE_REQUIRED: return "A new version is available. Please update the app."
        }
    }

    public var isHandledOnDomain: Bool {
        true
    }
}
