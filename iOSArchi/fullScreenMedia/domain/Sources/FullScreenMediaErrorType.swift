import CommonDomain
import Foundation

public enum FullScreenMediaErrorType: CaseIterable, HTTPErrorType {
    case mediaNotAvailable

    public var type: String {
        switch self {
        case .mediaNotAvailable: return "api.fullscreen.media_not_available"
        }
    }

    public var errorMsg: String {
        switch self {
        case .mediaNotAvailable: return "Media is not available."
        }
    }

    public var isHandledOnDomain: Bool {
        true
    }
}
