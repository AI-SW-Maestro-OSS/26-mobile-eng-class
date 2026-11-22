import Foundation
import TTI

public struct FullScreenMediaTTIPage: TTIPage {
    public static let shared = FullScreenMediaTTIPage()

    public init() {}

    public var pageName: String { "FullScreenMediaPage" }

    public var timelines: [TimelineCategory] {
        [
            .ttiTime,
            .viewCreationTime,
            .apiRequestReadyTime,
            .apiResponseTime,
            .viewBindingTime,
            .imageLoadedTime
        ]
    }
}
