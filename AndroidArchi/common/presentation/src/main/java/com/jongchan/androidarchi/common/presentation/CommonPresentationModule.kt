package com.jongchan.androidarchi.common.presentation

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import com.jongchan.androidarchi.common.domain.coroutine.TtiDispatcher
import com.jongchan.androidarchi.common.domain.helper.MessageHelper
import com.jongchan.androidarchi.common.domain.helper.NavigationHelper
import com.jongchan.androidarchi.common.domain.helper.ResourceHelper
import com.jongchan.androidarchi.common.presentation.helper.MessageHelperImpl
import com.jongchan.androidarchi.common.presentation.helper.NavigationHelperImpl
import com.jongchan.androidarchi.common.presentation.helper.ResourceHelperImpl
import com.jongchan.androidarchi.tti.DebugTTILogger
import com.jongchan.androidarchi.tti.NoOpTTIReporter
import com.jongchan.androidarchi.tti.RemoteTTILogger
import com.jongchan.androidarchi.tti.TTIHelper
import com.jongchan.androidarchi.tti.TTIHelperImpl
import com.jongchan.androidarchi.tti.TTILogger
import com.jongchan.androidarchi.tti.TTIReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
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
    fun provideTTIReporter(): TTIReporter = NoOpTTIReporter

    @Provides
    @Singleton
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

val LocalTTIHelper = compositionLocalOf<TTIHelper> {
    error("LocalTTIHelper is not provided. Wrap with CompositionLocalProvider in MainActivity.")
}