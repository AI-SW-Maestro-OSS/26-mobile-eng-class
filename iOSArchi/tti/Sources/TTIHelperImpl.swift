import Foundation

public final class TTIHelperImpl: TTIHelper, @unchecked Sendable {
    public static let ttiTimeoutMilliseconds: UInt64 = 20_000

    private static let counterLock = NSLock()
    private static var instanceCounters: [String: Int64] = [:]

    private static func nextInstanceNo(pageName: String) -> Int64 {
        counterLock.lock()
        defer { counterLock.unlock() }
        let next = (instanceCounters[pageName] ?? 0) + 1
        instanceCounters[pageName] = next
        return next
    }

    private let reporter: TTIReporter
    private let logger: TTILogger
    private let queue: DispatchQueue
    private let stateLock = NSLock()
    private var ttiInfo: TTIInfo?
    private var isReleased: Bool = false
    private var timeoutWorkItem: DispatchWorkItem?

    public init(
        reporter: TTIReporter = NoOpTTIReporter.shared,
        logger: TTILogger = DebugTTILogger(),
        queue: DispatchQueue = DispatchQueue(label: "com.jongchan.iosarchi.tti.serial")
    ) {
        self.reporter = reporter
        self.logger = logger
        self.queue = queue
    }

    public func startTTITracking(_ page: TTIPage) {
        stateLock.lock()
        if ttiInfo != nil {
            stateLock.unlock()
            logger.debug(category: "TTI", message: "startTTITracking() called twice. Page: \(page.pageName)")
            return
        }
        let info = TTIInfo(page: page, instanceNo: Self.nextInstanceNo(pageName: page.pageName))
        self.ttiInfo = info
        stateLock.unlock()

        queue.async { [weak self] in
            guard let self, !self.isReleased else { return }
            self.reporter.startView(key: info.ttiKey, name: page.pageName, attributes: [:])
            info.recordStartTime(.ttiTime)
            self.logger.debug(
                category: "TTI",
                message: "Start TTI Tracking : \(info.ttiKey) / \(page.pageName)"
            )
            self.scheduleTimeout(info)
        }
    }

    private func scheduleTimeout(_ info: TTIInfo) {
        let workItem = DispatchWorkItem { [weak self] in
            guard let self, !self.isReleased else { return }
            if info.isCanRecordTimeout() {
                info.allTTIRecordedFlag = true
                info.isSent = true
                let reportInfo = info.getTTIInfo()
                self.reporter.stopView(key: info.ttiKey, attributes: reportInfo)
                self.logger.debug(category: "TTI", message: "Timeout TTI Tracking \(reportInfo)")
                self.release()
            }
        }
        self.timeoutWorkItem = workItem
        let delaySeconds = Double(Self.ttiTimeoutMilliseconds) / 1000.0
        queue.asyncAfter(deadline: .now() + delaySeconds, execute: workItem)
    }

    private func release() {
        isReleased = true
        timeoutWorkItem?.cancel()
        timeoutWorkItem = nil
    }

    private func launchIfStarted(_ block: @escaping @Sendable (TTIInfo) -> Void) {
        stateLock.lock()
        guard let info = ttiInfo else {
            stateLock.unlock()
            return
        }
        stateLock.unlock()

        queue.async { [weak self] in
            guard let self, !self.isReleased else { return }
            block(info)
        }
    }

    public func startTTITimeline(_ category: TimelineCategory) {
        launchIfStarted { info in
            if info.allTTIRecordedFlag { return }
            info.recordStartTime(category)
        }
    }

    public func endTTITimeline(_ category: TimelineCategory) {
        launchIfStarted { info in
            if info.allTTIRecordedFlag { return }
            info.recordEndTime(category)
        }
    }

    public func endTTITracking() {
        launchIfStarted { [weak self] info in
            guard let self else { return }
            if info.cantEndTTITracking() { return }
            info.timeoutFlag = false
            info.allTTIRecordedFlag = true
            info.recordEndTime(.ttiTime)
            self.timeoutWorkItem?.cancel()
            self.logger.debug(category: "TTI", message: "End TTI Tracking : \(info.ttiKey)")
        }
    }

    public func shotTTILogging() {
        launchIfStarted { [weak self] info in
            guard let self else { return }
            if info.isSent { return }
            info.isSent = true
            let reportInfo = info.getTTIInfo()
            self.reporter.stopView(key: info.ttiKey, attributes: reportInfo)
            self.logger.debug(category: "TTI", message: "Shot TTI Logging : \(info.ttiKey) / \(reportInfo)")
            self.release()
        }
    }

    public func addTTIMetaData(_ metadata: TTIMetaData, value: Any?) {
        let box = SendableBox(value: value)
        launchIfStarted { info in
            if info.isSent { return }
            info.addTTIMetaData(metadata, value: box.value)
        }
    }
}

private struct SendableBox: @unchecked Sendable {
    let value: Any?
}
