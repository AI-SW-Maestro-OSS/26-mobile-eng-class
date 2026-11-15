import CommonDomain
import Foundation

public final class DebugJankReport: JankReport, @unchecked Sendable {
    public init() {}

    public func onSnapshot(_ snapshot: JankSnapshot) {
        let ratioPercent = String(format: "%.2f%%", snapshot.jankRatio * 100.0)
        let p90 = String(format: "%.2fms", snapshot.p90Ms)
        let p95 = String(format: "%.2fms", snapshot.p95Ms)
        let maxMs = String(format: "%.2fms", snapshot.maxMs)
        print(
            "[JankReport][\(snapshot.page)] total=\(snapshot.totalFrames), jank=\(snapshot.jankFrames) (\(ratioPercent)), p90=\(p90), p95=\(p95), max=\(maxMs), states=\(snapshot.stateCounts)"
        )
    }
}
