import Foundation

public let TTI_LOG_VERSION: Int = 1
public let TTI_PREFIX: String = "tti."

public final class TTIInfo: @unchecked Sendable {
    private let page: TTIPage
    public let instanceNo: Int64
    private var ttiTimelineMap: [String: Timeline] = [:]
    public var timeoutFlag: Bool = true
    public var allTTIRecordedFlag: Bool = false
    public var isSent: Bool = false

    // 같은 페이지에 대한 넘버링을 추가하여 구분할 수 있도록 한다.
    public let ttiKey: String
    private var additionalMetaData: [String: Any] = [:]

    public init(page: TTIPage, instanceNo: Int64 = 1) {
        self.page = page
        self.instanceNo = instanceNo
        let currentMillis = Int64(Date().timeIntervalSince1970 * 1000)
        self.ttiKey = "\(page.pageName)#\(instanceNo)_\(currentMillis)"
    }

    public func recordStartTime(_ ttiTime: TimelineCategory) {
        if var timeline = ttiTimelineMap[ttiTime.categoryName] {
            if timeline.startTime == 0 {
                timeline.startTime = currentEpochTimeInNano()
                ttiTimelineMap[ttiTime.categoryName] = timeline
            }
        } else {
            ttiTimelineMap[ttiTime.categoryName] = Timeline(
                startTime: currentEpochTimeInNano()
            )
        }
    }

    public func recordEndTime(_ ttiTime: TimelineCategory) {
        if var timeline = ttiTimelineMap[ttiTime.categoryName] {
            if timeline.endTime == 0 {
                timeline.endTime = currentEpochTimeInNano()
                ttiTimelineMap[ttiTime.categoryName] = timeline
            }
        } else {
            ttiTimelineMap[ttiTime.categoryName] = Timeline(
                endTime: currentEpochTimeInNano()
            )
        }
    }

    public func getTTIInfo() -> [String: Any] {
        var ttiInfo: [String: Any] = [:]
        initTTIData(&ttiInfo)
        updateTTITimeline(&ttiInfo)
        for (k, v) in additionalMetaData {
            ttiInfo[k] = v
        }
        return ttiInfo
    }

    private func initTTIData(_ ttiInfo: inout [String: Any]) {
        ttiInfo[TTI_PREFIX + TTIMetaData.pageName.metadataName] = page.pageName
        ttiInfo[TTI_PREFIX + TTIMetaData.instanceNo.metadataName] = instanceNo
        ttiInfo[TTI_PREFIX + TTIMetaData.isBounced.metadataName] = false
        ttiInfo[TTI_PREFIX + TTIMetaData.ttiLogVersion.metadataName] = TTI_LOG_VERSION
        ttiInfo[TTI_PREFIX + TimelineCategory.ttiTime.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.viewCreationTime.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.apiRequestReadyTime.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.apiResponseTime.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.viewBindingTime.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.imageLoadedTime.categoryName] = -1
    }

    private func updateTTITimeline(_ ttiInfo: inout [String: Any]) {
        let expectedTimelineCount = page.timelines.count
        ttiInfo[TTI_PREFIX + TTIMetaData.isBounced.metadataName] =
            ttiTimelineMap.count < expectedTimelineCount

        for (timelineKey, timeline) in ttiTimelineMap {
            let duration = timeline.endTime - timeline.startTime
            if duration < 0 {
                ttiInfo[TTI_PREFIX + TTIMetaData.isBounced.metadataName] = true
                ttiInfo[TTI_PREFIX + timelineKey] = -1
            } else {
                ttiInfo[TTI_PREFIX + timelineKey] = duration
            }
        }
    }

    public func isCanRecordTimeout() -> Bool {
        timeoutFlag && !allTTIRecordedFlag
    }

    public func cantEndTTITracking() -> Bool {
        allTTIRecordedFlag || !isRecordedLastTimeline()
    }

    private func isRecordedLastTimeline() -> Bool {
        guard let lastTimelineForPage = page.timelines.last?.categoryName,
              let timeline = ttiTimelineMap[lastTimelineForPage] else {
            return false
        }
        return timeline.endTime != 0 && timeline.startTime != 0
    }

    public func addTTIMetaData(_ metadata: TTIMetaData, value: Any?) {
        if let value {
            additionalMetaData[TTI_PREFIX + metadata.metadataName] = value
        }
    }
}

private func currentEpochTimeInNano() -> Int64 {
    Int64(Date().timeIntervalSince1970 * 1_000_000_000)
}
