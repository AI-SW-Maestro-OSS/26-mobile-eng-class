package com.jongchan.androidarchi.tti

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class TTIHelperImpl(
    private val reporter: TTIReporter = NoOpTTIReporter,
    private val logger: TTILogger,
    // 순서 보장은 주입된 직렬 디스패처(공용 FIFO 레인)가 담당하고, 인스턴스별 scope를 생성함으로써 페이지 간 순서 꼬임을 방지한다.
    dispatcher: CoroutineDispatcher,
) : TTIHelper {

    companion object {
        const val TTI_TIMEOUT_MILLISECONDS = 20000L

        // 페이지별 인스턴스 번호. 인스턴스(ViewModel)별이 아니라 프로세스 전역이어야 하므로 companion 에 둔다.
        // 단조 증가만 한다. 특정 페이지 트래킹 종료 후 절대 인스턴스 번호를 감소시켜서는 안된다.
        private val instanceCounters = ConcurrentHashMap<String, AtomicLong>()

        private fun nextInstanceNo(pageName: String): Long =
            instanceCounters
                .computeIfAbsent(pageName) { AtomicLong(0L) }
                .incrementAndGet()
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        logger.d(tag = "TTI", msg = "Uncaught exception: ${e.message}")
    }

    // 이 인스턴스(= ViewModel 1개)의 측정 전용 scope. 리포트 발사(shot/timeout) 후 cancel 된다.
    // SupervisorJob으로 한 이유는 형제 코루틴 간 에러 전파를 막기 위함! 특정 트래킹이 실패했을 때 전체 트래킹이 중단되는 것을 방지
    private val scope = CoroutineScope(SupervisorJob() + dispatcher + exceptionHandler)

    // null = 아직 startTTITracking 전
    @Volatile
    private var ttiInfo: TTIInfo? = null

    override fun startTTITracking(page: TTIPage) {
        if(ttiInfo != null) { // 개발 중에 잘못된 호출이 들어온 경우 에러 로그를 띄워주고 조용히 종료한다.(중복 호출 무시)
            logger.d("TTI", "startTTITracking() called twice. Page: ${page.pageName}")
            return
        }
        val ttiInfo = TTIInfo(page, nextInstanceNo(page.pageName))
        this.ttiInfo = ttiInfo
        scope.launch {
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
            // 타임아웃 발사 = 이 인스턴스의 유일한 리포트. 이후 shotTTILogging 은 무시된다.
            ttiInfo.isSent = true
            try {
                val info = ttiInfo.getTTIInfo()
                reporter.stopView(key = ttiInfo.ttiKey, info)
                logger.d(tag = "TTI", msg = "Timeout TTI Tracking $info")
            } finally {
                // 자기 scope 의 cancel 을 포함하므로 반드시 코루틴의 마지막 문장이어야 한다.
                release()
            }
        }
        // endTTITracking 이 이미 온 정상 케이스: scope 는 페이지 이탈(shotTTILogging)까지 유지된다.
    }

    // cancel 이후의 launch 는 실행되지 않으므로, 발사 뒤 늦게 도착한 호출은 자연스럽게 무시된다.
    private fun release() {
        scope.cancel()
    }

    // 트래킹 시작 전이면 무시하고, 시작 후면 인스턴스 scope(직렬 레인)에서 실행한다.
    private fun launchIfStarted(block: suspend (TTIInfo) -> Unit) {
        val ttiInfo = ttiInfo ?: return
        scope.launch { block(ttiInfo) }
    }

    override fun startTTITimeline(category: TimelineCategory) = launchIfStarted { ttiInfo ->
        if (ttiInfo.allTTIRecordedFlag) {
            return@launchIfStarted
        }
        ttiInfo.recordStartTime(category)
    }

    override fun endTTITimeline(category: TimelineCategory) = launchIfStarted { ttiInfo ->
        if (ttiInfo.allTTIRecordedFlag) {
            return@launchIfStarted
        }
        ttiInfo.recordEndTime(category)
    }

    override fun endTTITracking() = launchIfStarted { ttiInfo ->
        if (ttiInfo.cantEndTTITracking()) {
            return@launchIfStarted
        }
        ttiInfo.timeoutFlag = false
        ttiInfo.allTTIRecordedFlag = true
        ttiInfo.recordEndTime(TimelineCategory.TTI_TIME)
        logger.d(tag = "TTI", msg = "End TTI Tracking : ${ttiInfo.ttiKey}")
    }

    override fun shotTTILogging() = launchIfStarted { ttiInfo ->
        if (ttiInfo.isSent) {
            return@launchIfStarted
        }
        ttiInfo.isSent = true
        try {
            val info = ttiInfo.getTTIInfo()
            reporter.stopView(key = ttiInfo.ttiKey, info)
            logger.d(tag = "TTI", msg = "Shot TTI Logging : ${ttiInfo.ttiKey} / $info")
        } finally {
            // 자기 scope 의 cancel 을 포함하므로 반드시 코루틴의 마지막 문장이어야 한다.
            release()
        }
    }

    override fun addTTIMetaData(metadata: TTIMetaData, value: Any?) = launchIfStarted { ttiInfo ->
        if (ttiInfo.isSent) {
            return@launchIfStarted
        }
        ttiInfo.addTTIMetaData(metadata, value)
    }
}
