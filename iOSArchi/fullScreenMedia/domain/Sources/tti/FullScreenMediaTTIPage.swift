import Foundation
import TTI

public struct FullScreenMediaTTIPage: TTIPage {
    public static let shared = FullScreenMediaTTIPage()

    public init() {}

    public var pageName: String { "FullScreenMediaPage" }

    public var timelines: [TimelineCategory] {
        [
            .TTI_TIME,
            .VIEW_CREATION_TIME,
            .API_REQUEST_READY_TIME,
            .API_RESPONSE_TIME,
            .VIEW_BINDING_TIME,
            .IMAGE_LOADED_TIME
        ]
    }
}
