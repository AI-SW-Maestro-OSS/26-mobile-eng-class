package com.jongchan.androidarchi.common.data.analytics.tti

import com.jongchan.androidarchi.common.data.BuildConfig
import com.jongchan.androidarchi.common.domain.analytics.tti.TtiLoggingConfig
import com.jongchan.androidarchi.common.domain.analytics.tti.TtiLoggingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * TTI Logging(Google Apps Script Web App) 전용 네트워크 스택.
 *
 * 유저 행동 Metric 로깅(MetricLoggingDataModule)과 코드·설정 키가 분리되어 있다 — 엔드포인트/토큰은
 * `TTI_LOG_URL`/`TTI_LOG_TOKEN`(TTI-LOG-INJECTION-POINT)으로 따로 주입된다. 같은 시트를 쓰려면 두 키에
 * METRIC_LOG_* 와 같은 값을 넣고 시트에 `tti_report` 탭만 추가하면 되고, TTI 만 다른 시트/백엔드로
 * 옮길 때는 이 값만 바꾸면 된다. 기본 [Retrofit] 은 카카오 OpenAPI baseUrl + 인증 인터셉터가 붙어 있어
 * 공유하지 않는다 (docs/architecture/data-layer.md 의 @Named 분리 규칙).
 */
@Module
@InstallIn(SingletonComponent::class)
object TtiLoggingDataModule {

    const val NAMED_TTI_LOGGING = "ttiLogging"

    @Provides
    @Singleton
    fun provideTtiLoggingConfig(): TtiLoggingConfig = TtiLoggingConfig(
        isEnabled = BuildConfig.TTI_LOG_URL.isNotBlank() && BuildConfig.TTI_LOG_TOKEN.isNotBlank(),
    )

    @Provides
    @Singleton
    fun provideTtiLoggingRepository(dataSource: TtiLoggingDataSource): TtiLoggingRepository =
        TtiLoggingRepositoryImpl(
            dataSource = dataSource,
            token = BuildConfig.TTI_LOG_TOKEN,
        )

    @Provides
    @Singleton
    fun provideTtiLoggingDataSource(apiService: TtiLoggingApiService): TtiLoggingDataSource =
        TtiLoggingDataSource(
            apiService = apiService,
            endpointUrl = BuildConfig.TTI_LOG_URL,
        )

    @Provides
    @Singleton
    fun provideTtiLoggingApiService(
        @Named(NAMED_TTI_LOGGING) retrofit: Retrofit,
    ): TtiLoggingApiService = retrofit.create(TtiLoggingApiService::class.java)

    @Provides
    @Singleton
    @Named(NAMED_TTI_LOGGING)
    fun provideTtiLoggingOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // Apps Script 는 POST 에 302 로 응답하고, 리다이렉트된 GET 이 실제 응답 본문을 돌려준다.
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named(NAMED_TTI_LOGGING)
    fun provideTtiLoggingRetrofit(
        @Named(NAMED_TTI_LOGGING) okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            // 실제 요청 URL 은 @Url 로 넘기므로 baseUrl 은 호스트 자리만 채운다.
            .baseUrl(PLACEHOLDER_BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(okHttpClient)
            .build()
    }

    private const val PLACEHOLDER_BASE_URL = "https://script.google.com/"
}
