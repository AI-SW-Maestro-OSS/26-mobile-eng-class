package com.jongchan.androidarchi.common.data.analytics.metric

import com.jongchan.androidarchi.common.data.BuildConfig
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingConfig
import com.jongchan.androidarchi.common.domain.analytics.metric.MetricLoggingRepository
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
 * Metric Logging(Google Apps Script Web App) 전용 네트워크 스택.
 *
 * 기본 [Retrofit] 은 카카오 OpenAPI baseUrl + `KakaoAK` 인증 인터셉터가 붙어 있어 공유하지 않는다
 * (API 키가 Apps Script 로 새어 나가면 안 되고, 호스트도 다르다). docs/architecture/data-layer.md 의
 * "새 API 호스트가 추가되면 Retrofit 인스턴스를 @Named 로 분리 제공" 규칙을 따른다.
 *
 * METRIC-LOG-INJECTION-POINT: 엔드포인트/토큰은 local.properties → common/data BuildConfig 로 주입된다.
 */
@Module
@InstallIn(SingletonComponent::class)
object MetricLoggingDataModule {

    const val NAMED_METRIC_LOGGING = "metricLogging"

    @Provides
    @Singleton
    fun provideMetricLoggingConfig(): MetricLoggingConfig = MetricLoggingConfig(
        isEnabled = BuildConfig.METRIC_LOG_URL.isNotBlank() && BuildConfig.METRIC_LOG_TOKEN.isNotBlank(),
    )

    @Provides
    @Singleton
    fun provideMetricLoggingRepository(dataSource: MetricLoggingDataSource): MetricLoggingRepository =
        MetricLoggingRepositoryImpl(
            dataSource = dataSource,
            token = BuildConfig.METRIC_LOG_TOKEN,
        )

    @Provides
    @Singleton
    fun provideMetricLoggingDataSource(apiService: MetricLoggingApiService): MetricLoggingDataSource =
        MetricLoggingDataSource(
            apiService = apiService,
            endpointUrl = BuildConfig.METRIC_LOG_URL,
        )

    @Provides
    @Singleton
    fun provideMetricLoggingApiService(
        @Named(NAMED_METRIC_LOGGING) retrofit: Retrofit,
    ): MetricLoggingApiService = retrofit.create(MetricLoggingApiService::class.java)

    @Provides
    @Singleton
    @Named(NAMED_METRIC_LOGGING)
    fun provideMetricLoggingOkHttpClient(): OkHttpClient {
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
    @Named(NAMED_METRIC_LOGGING)
    fun provideMetricLoggingRetrofit(
        @Named(NAMED_METRIC_LOGGING) okHttpClient: OkHttpClient,
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
