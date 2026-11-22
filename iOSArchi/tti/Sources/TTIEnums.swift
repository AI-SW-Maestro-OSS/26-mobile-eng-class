import Foundation

public enum TimelineCategory: String, CaseIterable, Sendable {
    case ttiTime = "tti_time"
    case viewCreationTime = "view_creation_time"
    case apiRequestReadyTime = "api_request_ready_time"
    case apiResponseTime = "api_response_time"
    case viewBindingTime = "view_binding_time"
    case imageLoadedTime = "image_loaded_time"

    public var categoryName: String { rawValue }
}

public enum TTIMetaData: String, CaseIterable, Sendable {
    case pageName = "page_name"
    case instanceNo = "instance_no"
    case isBounced = "is_bounced"
    case isTimeout = "is_timeout"
    case ttiLogVersion = "tti_log_version"

    public var metadataName: String { rawValue }
}
