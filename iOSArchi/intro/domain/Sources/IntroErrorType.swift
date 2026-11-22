import CommonDomain
import Foundation

public enum IntroErrorType: CaseIterable, HTTPErrorType {
    case invalidToken
    case maintenance
    case forceUpdateRequired

    public var type: String {
        switch self {
        case .invalidToken: return "api.intro.invalid_token"
        case .maintenance: return "api.intro.maintenance"
        case .forceUpdateRequired: return "api.intro.force_update_required"
        }
    }

    public var errorMsg: String {
        switch self {
        case .invalidToken: return "Authentication failed. Please sign in again."
        case .maintenance: return "Server is currently under maintenance."
        case .forceUpdateRequired: return "A new version is available. Please update the app."
        }
    }

    public var isHandledOnDomain: Bool {
        true
    }
}
