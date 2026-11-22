import CommonDomain
import Foundation

public enum SearchErrorType: CaseIterable, HTTPErrorType {
    case invalidQuery
    case rateLimitExceeded

    public var type: String {
        switch self {
        case .invalidQuery: return "api.search.invalid_query"
        case .rateLimitExceeded: return "api.search.rate_limit_exceeded"
        }
    }

    public var errorMsg: String {
        switch self {
        case .invalidQuery: return "Invalid search query."
        case .rateLimitExceeded: return "Search rate limit exceeded. Please try again shortly."
        }
    }

    public var isHandledOnDomain: Bool {
        true
    }
}
