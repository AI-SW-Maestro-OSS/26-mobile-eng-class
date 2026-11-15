import Foundation

public struct JankSnapshot: Sendable, Equatable {
    public let page: String
    public let totalFrames: Int
    public let jankFrames: Int
    public let jankRatio: Double
    public let p90Ms: Double
    public let p95Ms: Double
    public let maxMs: Double
    public let stateCounts: [String: Int]

    public init(
        page: String,
        totalFrames: Int,
        jankFrames: Int,
        jankRatio: Double,
        p90Ms: Double,
        p95Ms: Double,
        maxMs: Double,
        stateCounts: [String: Int] = [:]
    ) {
        self.page = page
        self.totalFrames = totalFrames
        self.jankFrames = jankFrames
        self.jankRatio = jankRatio
        self.p90Ms = p90Ms
        self.p95Ms = p95Ms
        self.maxMs = maxMs
        self.stateCounts = stateCounts
    }
}

public protocol JankReport: Sendable {
    func onSnapshot(_ snapshot: JankSnapshot)
}
