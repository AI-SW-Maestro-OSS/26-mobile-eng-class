import CommonDomain
import Foundation

public enum SearchErrorType: CaseIterable, HttpErrorType {
    case INVALID_QUERY
    case RATE_LIMIT_EXCEEDED

    public var type: String {
        switch self {
        case .INVALID_QUERY: return "api.search.invalid_query"
        case .RATE_LIMIT_EXCEEDED: return "api.search.rate_limit_exceeded"
        }
    }

    public var errorMsg: String {
        switch self {
        case .INVALID_QUERY: return "Invalid search query."
        case .RATE_LIMIT_EXCEEDED: return "Search rate limit exceeded. Please try again shortly."
        }
    }

    public var isHandledOnDomain: Bool {
        true
    }
}
