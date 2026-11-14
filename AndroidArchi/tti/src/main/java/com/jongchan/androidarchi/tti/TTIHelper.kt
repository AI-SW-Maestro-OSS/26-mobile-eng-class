package com.jongchan.androidarchi.tti

/**
 * 한 화면 인스턴스(= ViewModel 1개)의 TTI 측정 핸들.
 *
 * Hilt 의 ViewModelComponent 에 @ViewModelScoped 로 바인딩되어 ViewModel 인스턴스당
 * 정확히 하나 생성되고, 같은 ViewModel 에 주입되는 UseCase 들과 인스턴스를 공유한다.
 * - ViewModel: init 에서 [startTTITracking] 을 호출해 트래킹을 시작한다.
 * - UseCase / View: 같은 인스턴스로 타임라인을 기록한다.
 */
interface TTIHelper {
    fun startTTITracking(page: TTIPage)
    fun startTTITimeline(category: TimelineCategory)
    fun endTTITimeline(category: TimelineCategory)
    fun endTTITracking()
    fun shotTTILogging()
    fun addTTIMetaData(metadata: TTIMetaData, value: Any?)
}
