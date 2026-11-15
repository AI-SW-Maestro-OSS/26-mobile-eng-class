import Foundation

public enum TimelineCategory: String, CaseIterable, Sendable {
    case TTI_TIME = "tti_time"
    case VIEW_CREATION_TIME = "view_creation_time"
    case API_REQUEST_READY_TIME = "api_request_ready_time"
    case API_RESPONSE_TIME = "api_response_time"
    case VIEW_BINDING_TIME = "view_binding_time"
    case IMAGE_LOADED_TIME = "image_loaded_time"

    public var categoryName: String { rawValue }
}

public enum TTIMetaData: String, CaseIterable, Sendable {
    case PAGE_NAME = "page_name"
    case INSTANCE_NO = "instance_no"
    case IS_BOUNCED = "is_bounced"
    case IS_TIMEOUT = "is_timeout"
    case TTI_LOG_VERSION = "tti_log_version"

    public var metadataName: String { rawValue }
}
