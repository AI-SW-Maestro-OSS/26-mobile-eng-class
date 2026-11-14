package com.jongchan.androidarchi.tti

const val TTI_LOG_VERSION = 1
const val TTI_PREFIX = "tti."

class TTIInfo(
    private val page: TTIPage,
    val instanceNo: Long = 1L,
) {
    private val ttiTimelineMap = mutableMapOf<String, Timeline?>()
    var timeoutFlag = true
    var allTTIRecordedFlag = false
    var isSent = false

    // 같은 페이지에 대한 넘버링을 추가하여 구분할 수 있도록 한다.
    val ttiKey: String = "${page.pageName}#${instanceNo}_${System.currentTimeMillis()}"
    private val additionalMetaData = mutableMapOf<String, Any?>()

    fun recordStartTime(ttiTime: TimelineCategory) {
        ttiTimelineMap[ttiTime.categoryName]?.let { timeline ->
            if (timeline.startTime == 0L) {
                ttiTimelineMap[ttiTime.categoryName] = timeline.copy(
                    startTime = currentEpochTimeInNano(),
                )
            }
        } ?: run {
            ttiTimelineMap[ttiTime.categoryName] = Timeline(
                startTime = currentEpochTimeInNano(),
            )
        }
    }

    fun recordEndTime(ttiTime: TimelineCategory) {
        ttiTimelineMap[ttiTime.categoryName]?.let { timeline ->
            if (timeline.endTime == 0L) {
                ttiTimelineMap[ttiTime.categoryName] = timeline.copy(
                    endTime = currentEpochTimeInNano(),
                )
            }
        } ?: run {
            ttiTimelineMap[ttiTime.categoryName] = Timeline(
                endTime = currentEpochTimeInNano(),
            )
        }
    }

    fun getTTIInfo(): Map<String, Any?> {
        val ttiInfo = mutableMapOf<String, Any?>()
        initTTIData(ttiInfo)
        updateTTITimeline(ttiInfo)
        ttiInfo.putAll(additionalMetaData)
        return ttiInfo
    }

    private fun initTTIData(ttiInfo: MutableMap<String, Any?>) {
        ttiInfo[TTI_PREFIX + TTIMetaData.PAGE_NAME.metadataName] = page.pageName
        ttiInfo[TTI_PREFIX + TTIMetaData.INSTANCE_NO.metadataName] = instanceNo
        ttiInfo[TTI_PREFIX + TTIMetaData.IS_BOUNCED.metadataName] = false
        ttiInfo[TTI_PREFIX + TTIMetaData.TTI_LOG_VERSION.metadataName] = TTI_LOG_VERSION
        ttiInfo[TTI_PREFIX + TimelineCategory.TTI_TIME.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.VIEW_CREATION_TIME.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.API_REQUEST_READY_TIME.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.API_RESPONSE_TIME.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.VIEW_BINDING_TIME.categoryName] = -1
        ttiInfo[TTI_PREFIX + TimelineCategory.IMAGE_LOADED_TIME.categoryName] = -1
    }

    private fun updateTTITimeline(ttiInfo: MutableMap<String, Any?>) {
        val expectedTimelineCount = page.timelines.size
        ttiInfo[TTI_PREFIX + TTIMetaData.IS_BOUNCED.metadataName] =
            ttiTimelineMap.size < expectedTimelineCount

        val timelineEntries = ttiTimelineMap.entries.toList()
        for ((timelineKey, timeline) in timelineEntries) {
            timeline?.let {
                val duration = it.endTime - it.startTime
                if (duration < 0L) {
                    ttiInfo[TTI_PREFIX + TTIMetaData.IS_BOUNCED.metadataName] = true
                    ttiInfo[TTI_PREFIX + timelineKey] = -1
                } else {
                    ttiInfo[TTI_PREFIX + timelineKey] = duration
                }
            }
        }
    }

    fun isCanRecordTimeout(): Boolean {
        return timeoutFlag && allTTIRecordedFlag.not()
    }

    fun cantEndTTITracking(): Boolean {
        return allTTIRecordedFlag || isRecordedLastTimeline().not()
    }

    private fun isRecordedLastTimeline(): Boolean {
        val lastTimelineForPage = page.timelines.last().categoryName
        ttiTimelineMap[lastTimelineForPage]?.let {
            return it.endTime != 0L && it.startTime != 0L
        }
        return false
    }

    fun addTTIMetaData(metadata: TTIMetaData, value: Any?) {
        additionalMetaData[TTI_PREFIX + metadata.metadataName] = value
    }
}

private fun currentEpochTimeInNano(): Long = System.currentTimeMillis() * 1_000_000L
