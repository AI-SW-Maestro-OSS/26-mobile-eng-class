package com.jongchan.androidarchi.tti

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TTIHelperImpl(
    private val reporter: TTIReporter = NoOpTTIReporter,
    private val logger: TTILogger,
    // TtiDispatcher : tti event 순서대로 처리되도록 Dispatcher.limitedParallelism(1)로
    // 제한된 디스패처를 주입받는다. 동시에 살아있는 코루틴은 1개만 존재하도록 제한한다.
    dispatcher: CoroutineDispatcher,
) : TTIHelper {
    private var pageTTIMap = mutableMapOf<String, TTIInfo>()
    private val scope = CoroutineScope(
        SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, e ->
            logger.d(tag = "TTI", msg = "Uncaught exception: ${e.message}")
        }
    )

    companion object {
        const val TTI_TIMEOUT_MILLISECONDS = 20000L
    }

    override fun startTTITracking(page: TTIPage) {
        val ttiInfo = TTIInfo(page)
        scope.launch {
            pageTTIMap[page.pageName] = ttiInfo
            reporter.startView(ttiInfo.ttiKey, page.pageName, emptyMap())
            ttiInfo.recordStartTime(TimelineCategory.TTI_TIME)
            logger.d(
                tag = "TTI",
                msg = "Start TTI Tracking : ${ttiInfo.ttiKey} / ${page.pageName}",
            )
            doTimeoutTTI(ttiInfo)
        }
    }

    private suspend fun doTimeoutTTI(ttiInfo: TTIInfo) {
        delay(TTI_TIMEOUT_MILLISECONDS)
        if (ttiInfo.isCanRecordTimeout()) {
            ttiInfo.allTTIRecordedFlag = true
            val info = ttiInfo.getTTIInfo()
            reporter.stopView(key = ttiInfo.ttiKey, info)
            logger.d(tag = "TTI", msg = "Timeout TTI Tracking $info")
        }
    }

    override fun startTTITimeline(page: TTIPage, timelineCategory: TimelineCategory) {
        scope.launch {
            pageTTIMap[page.pageName]?.let {
                if (it.allTTIRecordedFlag) {
                    return@launch
                }
                it.recordStartTime(timelineCategory)
            }
        }
    }

    override fun endTTITimeline(page: TTIPage, timelineCategory: TimelineCategory) {
        scope.launch {
            pageTTIMap[page.pageName]?.let {
                if (it.allTTIRecordedFlag) {
                    return@launch
                }
                it.recordEndTime(timelineCategory)
            }
        }
    }

    override fun endTTITracking(page: TTIPage) {
        scope.launch {
            pageTTIMap[page.pageName]?.let {
                if (it.cantEndTTITracking()) {
                    return@launch
                }
                it.timeoutFlag = false
                it.allTTIRecordedFlag = true
                it.recordEndTime(TimelineCategory.TTI_TIME)
                logger.d(tag = "TTI", msg = "End TTI Tracking : ${it.ttiKey}")
            }
        }
    }

    override fun shotTTILogging(page: TTIPage) {
        scope.launch {
            pageTTIMap[page.pageName]?.let {
                if (it.isSent) {
                    return@launch
                }

                it.isSent = true
                val info = it.getTTIInfo()
                reporter.stopView(key = it.ttiKey, info)
                logger.d(tag = "TTI", msg = "Shot TTI Logging : ${it.ttiKey} / $info")
            }
        }
    }

    override fun addTTIMetaData(page: TTIPage, metadata: TTIMetaData, value: Any?) {
        // scope 바깥에서 Map 접근 시 동시성 문제 발생 가능성이 있어 scope 내부에서 처리하도록 변경
        scope.launch {
            pageTTIMap[page.pageName]?.addTTIMetaData(metadata, value)
        }
    }
}
