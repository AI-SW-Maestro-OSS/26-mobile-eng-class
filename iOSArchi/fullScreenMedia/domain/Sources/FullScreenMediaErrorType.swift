import CommonDomain
import Foundation

public enum FullScreenMediaErrorType: CaseIterable, HttpErrorType {
    case MEDIA_NOT_AVAILABLE

    public var type: String {
        switch self {
        case .MEDIA_NOT_AVAILABLE: return "api.fullscreen.media_not_available"
        }
    }

    public var errorMsg: String {
        switch self {
        case .MEDIA_NOT_AVAILABLE: return "Media is not available."
        }
    }

    public var isHandledOnDomain: Bool {
        true
    }
}
