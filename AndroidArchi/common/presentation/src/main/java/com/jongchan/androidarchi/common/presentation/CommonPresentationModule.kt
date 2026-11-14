package com.jongchan.androidarchi.common.presentation

import android.content.Context
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingConfig
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingRepository
import com.jongchan.androidarchi.common.domain.analytics.tti.TtiLoggingConfig
import com.jongchan.androidarchi.common.domain.analytics.tti.TtiLoggingRepository
import com.jongchan.androidarchi.common.domain.coroutine.IoScope
import com.jongchan.androidarchi.common.domain.coroutine.TtiDispatcher
import com.jongchan.androidarchi.common.domain.helper.LoggingHelper
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.presentation.helper.LoggingHelperImpl
import com.jongchan.androidarchi.common.presentation.helper.MessageHelperImpl
import com.jongchan.androidarchi.common.presentation.helper.NavigationHelperImpl
import com.jongchan.androidarchi.common.presentation.helper.ResourceHelperImpl
import com.jongchan.androidarchi.common.presentation.tti.SpreadsheetTTIReporter
import com.jongchan.androidarchi.tti.DebugTTILogger
import com.jongchan.androidarchi.tti.RemoteTTILogger
import com.jongchan.androidarchi.tti.TTIHelper
import com.jongchan.androidarchi.tti.TTIHelperImpl
import com.jongchan.androidarchi.tti.TTILogger
import com.jongchan.androidarchi.tti.TTIReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonPresentationModule {
    @Provides
    @Singleton
    fun provideMessageHelper(@ApplicationContext context: Context): MessageHelper =
        MessageHelperImpl(context)

    @Provides
    @Singleton
    fun provideLoggingHelper(
        metricLoggingRepository: MetricLoggingRepository,
        @IoScope ioScope: CoroutineScope,
        metricLoggingConfig: MetricLoggingConfig,
    ): LoggingHelper = LoggingHelperImpl(
        repository = metricLoggingRepository,
        scope = ioScope,
        isEnabled = metricLoggingConfig.isEnabled,
        isDebug = BuildConfig.DEBUG,
    )

    @Provides
    @Singleton
    fun provideNavigationHelper(): NavigationHelper = NavigationHelperImpl()

    @Provides
    @Singleton
    fun provideResourceHelper(@ApplicationContext context: Context): ResourceHelper =
        ResourceHelperImpl(context)

    @Provides
    @Singleton
    fun provideTTILogger(): TTILogger {
        return if (BuildConfig.DEBUG) DebugTTILogger() else RemoteTTILogger()
    }

    @Provides
    @Singleton
    fun provideTTIReporter(
        ttiLoggingRepository: TtiLoggingRepository,
        @IoScope ioScope: CoroutineScope,
        ttiLoggingConfig: TtiLoggingConfig,
    ): TTIReporter = SpreadsheetTTIReporter(
        repository = ttiLoggingRepository,
        scope = ioScope,
        isEnabled = ttiLoggingConfig.isEnabled,
    )
}

/**
 * ViewModel 인스턴스당 하나의 [TTIHelper] 를 보장한다.
 * 같은 ViewModel 생성 그래프에 참여하는 UseCase 들은
 * ViewModel과 동일한 TTIHelper 를 주입받는다.
 */
@Module
@InstallIn(ViewModelComponent::class)
object CommonViewModelModule {
    @Provides
    @ViewModelScoped
    fun provideTTIHelper(
        reporter: TTIReporter,
        logger: TTILogger,
        @TtiDispatcher ttiDispatcher: CoroutineDispatcher,
    ): TTIHelper = TTIHelperImpl(
        reporter = reporter,
        logger = logger,
        dispatcher = ttiDispatcher,
    )
}
