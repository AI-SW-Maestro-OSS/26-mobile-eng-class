import Foundation
#if canImport(QuartzCore)
import QuartzCore
#endif

public final class JankReporter: @unchecked Sendable {
    private let lock = NSLock()
    private let report: JankReport
    private var currentPage: String = "Unknown"
    private var currentStates: [String: String] = [:]
    private var durationsMs: [Double] = []
    private var jankFrames: Int = 0
    private var stateCounts: [String: Int] = [:]

    #if canImport(QuartzCore) && os(iOS)
    private var displayLink: CADisplayLink?
    private var lastTimestamp: CFTimeInterval = 0
    #endif

    public init(report: JankReport = DebugJankReport()) {
        self.report = report
    }

    public func startTracking(page: String) {
        lock.lock()
        if currentPage != "Unknown" && !durationsMs.isEmpty {
            let snapshot = buildSnapshotLocked()
            lock.unlock()
            report.onSnapshot(snapshot)
            lock.lock()
        }
        currentPage = page
        durationsMs.removeAll()
        jankFrames = 0
        stateCounts.removeAll()
        lock.unlock()

        #if canImport(QuartzCore) && os(iOS)
        DispatchQueue.main.async { [weak self] in
            self?.startDisplayLinkIfNeeded()
        }
        #endif
    }

    public func flushAndStop(page: String) {
        lock.lock()
        guard currentPage == page else {
            lock.unlock()
            return
        }
        let hasFrames = !durationsMs.isEmpty
        let snapshot = buildSnapshotLocked()
        durationsMs.removeAll()
        jankFrames = 0
        stateCounts.removeAll()
        lock.unlock()

        if hasFrames {
            report.onSnapshot(snapshot)
        }
    }

    public func putState(key: String, value: String) {
        lock.lock()
        defer { lock.unlock() }
        currentStates[key] = value
    }

    public func removeState(key: String) {
        lock.lock()
        defer { lock.unlock() }
        currentStates.removeValue(forKey: key)
    }

    public func recordFrame(durationMs: Double) {
        lock.lock()
        defer { lock.unlock() }
        durationsMs.append(durationMs)
        if durationMs > 16.67 {
            jankFrames += 1
            for (k, v) in currentStates {
                let stateKey = "\(k):\(v)"
                stateCounts[stateKey, default: 0] += 1
            }
        }
    }

    private func buildSnapshotLocked() -> JankSnapshot {
        let total = durationsMs.count
        guard total > 0 else {
            return JankSnapshot(
                page: currentPage,
                totalFrames: 0,
                jankFrames: 0,
                jankRatio: 0,
                p90Ms: 0,
                p95Ms: 0,
                maxMs: 0,
                stateCounts: [:]
            )
        }
        let sorted = durationsMs.sorted()
        let p90Index = min(total - 1, Int(Double(total) * 0.90))
        let p95Index = min(total - 1, Int(Double(total) * 0.95))
        let p90 = sorted[p90Index]
        let p95 = sorted[p95Index]
        let maxVal = sorted.last ?? 0.0
        let ratio = Double(jankFrames) / Double(total)
        return JankSnapshot(
            page: currentPage,
            totalFrames: total,
            jankFrames: jankFrames,
            jankRatio: ratio,
            p90Ms: p90,
            p95Ms: p95,
            maxMs: maxVal,
            stateCounts: stateCounts
        )
    }

    #if canImport(QuartzCore) && os(iOS)
    @MainActor
    private func startDisplayLinkIfNeeded() {
        guard displayLink == nil else { return }
        let link = CADisplayLink(target: self, selector: #selector(handleDisplayLink(_:)))
        link.add(to: .main, forMode: .common)
        displayLink = link
    }

    @objc private func handleDisplayLink(_ link: CADisplayLink) {
        if lastTimestamp > 0 {
            let deltaMs = (link.timestamp - lastTimestamp) * 1000.0
            if deltaMs > 0 && deltaMs < 1000.0 {
                recordFrame(durationMs: deltaMs)
            }
        }
        lastTimestamp = link.timestamp
    }
    #endif
}
