package com.jongchan.androidarchi.common.domain.coroutine

import javax.inject.Qualifier

/**
 * 앱 전역에서 IO 바운드 작업에 사용하는 디스패처. 내부적으로 [kotlinx.coroutines.Dispatchers.IO] 를
 * `limitedParallelism` 으로 감싸 동시 실행 쓰레드 수를 [com.jongchan.androidarchi.common.presentation.coroutine.CoroutineModule] 에
 * 정의된 상한 이하로 제한한다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * 앱 전역에서 UI 작업에 사용하는 디스패처. [kotlinx.coroutines.Dispatchers.Main] 의 별칭.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * 앱 전역에서 CPU 바운드 작업에 사용하는 디스패처. [kotlinx.coroutines.Dispatchers.Default] 의 별칭.
 *
 * `Default` 는 이미 코어 수에 맞춰 자체 제한되어 있으므로 [IoDispatcher] 와 달리
 * `limitedParallelism` 으로 추가 제한하지 않는다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * TTI 계측 이벤트 전용 직렬 디스패처. [IoDispatcher] 를 `limitedParallelism(1)` 로 감싸
 * 제출 순서대로(FIFO) 하나씩 실행된다
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TtiDispatcher

/**
 * IO 디스패처 위에 만든 앱 전역 [kotlinx.coroutines.CoroutineScope].
 * `SupervisorJob` + `CoroutineExceptionHandler` 가 포함되어 한 자식 코루틴의 실패가
 * 스코프 전체를 죽이지 않는다. 앱 프로세스 수명과 함께 살아 있으므로 별도 cancel 은 불필요.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoScope

/**
 * Main 디스패처 위에 만든 앱 전역 [kotlinx.coroutines.CoroutineScope].
 * 정책은 [IoScope] 와 동일.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainScope
