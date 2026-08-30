package com.jongchan.androidarchi.common.presentation.coroutine

import android.util.Log
import com.jongchan.androidarchi.common.domain.coroutine.DefaultDispatcher
import com.jongchan.androidarchi.common.domain.coroutine.IoDispatcher
import com.jongchan.androidarchi.common.domain.coroutine.IoScope
import com.jongchan.androidarchi.common.domain.coroutine.MainDispatcher
import com.jongchan.androidarchi.common.domain.coroutine.MainScope
import com.jongchan.androidarchi.common.domain.coroutine.TtiDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * 앱 전역 코루틴 인프라스트럭처를 제공한다.
 *
 * 설계 원칙
 * 1. **Dispatcher 는 싱글턴** — `Dispatchers.IO` 의 `limitedParallelism` 뷰는 호출마다 새 인스턴스를
 *    만들 수 있으므로, Hilt 로 한 번만 만들어 모든 호출자가 같은 인스턴스를 공유한다.
 *    이래야 IO 쓰레드 상한이 *앱 전역* 으로 의미를 가진다.
 * 2. **CoroutineScope 도 싱글턴** — `SupervisorJob` 과 `CoroutineExceptionHandler` 를 묶어
 *    한 자식 코루틴의 예외가 스코프 전체를 죽이지 않게 한다. 앱 프로세스 수명과 함께 살아있다.
 * 3. **IO 쓰레드 상한** — [MAX_IO_PARALLELISM] 으로 동시 실행 IO 작업 수를 제한한다.
 *    `Dispatchers.IO` 의 기본 풀(코어 수 또는 64 중 큰 값)을 그대로 두면 앱 모듈이 폭주 시
 *    OS 전체 리소스를 갉아먹을 수 있어, 의도된 상한을 두는 편이 안전하다.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    /**
     * 이 앱이 동시에 점유할 수 있는 IO 쓰레드 상한.
     *
     * `Dispatchers.IO` 의 기본 풀(`max(64, coreCount)`) 안에서 이 값만큼만 동시 실행되도록
     * `limitedParallelism` 으로 잘라낸다. 다른 라이브러리(OkHttp, Room 등)는 자체 풀을 가지므로
     * 영향받지 않는다.
     */
    private const val MAX_IO_PARALLELISM = 32

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(MAX_IO_PARALLELISM)

    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @TtiDispatcher
    fun provideTtiDispatcher(): CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(1)

    @Provides
    @Singleton
    @IoScope
    fun provideIoScope(
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(
        SupervisorJob() + dispatcher + uncaughtExceptionHandler(TAG_IO_SCOPE)
    )

    @Provides
    @Singleton
    @MainScope
    fun provideMainScope(
        @MainDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(
        SupervisorJob() + dispatcher + uncaughtExceptionHandler(TAG_MAIN_SCOPE)
    )

    private fun uncaughtExceptionHandler(tag: String): CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Log.e(tag, "Uncaught coroutine exception", throwable)
        }

    private const val TAG_IO_SCOPE = "AppIoScope"
    private const val TAG_MAIN_SCOPE = "AppMainScope"
}
