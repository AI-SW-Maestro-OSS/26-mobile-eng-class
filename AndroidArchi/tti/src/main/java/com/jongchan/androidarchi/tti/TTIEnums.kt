package com.jongchan.androidarchi.tti

enum class TimelineCategory(val categoryName: String) {
    TTI_TIME("tti_time"),
    VIEW_CREATION_TIME("view_creation_time"),
    API_REQUEST_READY_TIME("api_request_ready_time"),
    API_RESPONSE_TIME(
        "api_response_time",
    ),
    VIEW_BINDING_TIME("view_binding_time"),
    IMAGE_LOADED_TIME("image_loaded_time"),
}

enum class TTIMetaData(val metadataName: String) {
    PAGE_NAME("page_name"),
    INSTANCE_NO("instance_no"),
    IS_BOUNCED("is_bounced"),
    IS_TIMEOUT("is_timeout"),
    TTI_LOG_VERSION(
        "tti_log_version",
    ),
}
