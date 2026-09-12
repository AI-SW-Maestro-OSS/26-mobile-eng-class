package com.jongchan.androidarchi.common.data.remoteConfig

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.customSignals
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.jongchan.androidarchi.common.data.config.ApiEndpointProviderImpl
import com.jongchan.androidarchi.common.domain.config.ApiEndpoint
import com.jongchan.androidarchi.common.domain.config.ApiEndpointProvider
import com.jongchan.androidarchi.common.domain.coroutine.IoScope
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Remote Config. (prex-android `NetworkModule.provideRemoteConfig` + `RepositoryModule.provideRemoteConfigHelperV2` 이식)
 *
 * FIREBASE-CONFIG-INJECTION-POINT: `app/google-services.json` 이 없으면 `Firebase.remoteConfig` 가
 * "Default FirebaseApp is not initialized" 로 실패한다. 이 경우 [DefaultRemoteConfigRepository] 로 대체해
 * 앱은 정상 실행되고 모든 키는 defaultValue 를 돌려준다.
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigDataModule {
    private const val TAG = "RemoteConfigV2"

    // QA 이상(개발 환경)은 1분, 그 외(배포 환경)는 30분 간격으로만 서버 fetch 를 허용한다.
    private const val INTERVAL_BY_ONE_MINUTE = 60L
    private const val INTERVAL_BY_THIRTY_MINUTES = 60L * 30

    @Provides
    @Singleton
    fun provideApiEndpointProvider(): ApiEndpointProvider = ApiEndpointProviderImpl()

    @Provides
    @Singleton
    fun provideRemoteConfigRepositoryV2(
        @ApplicationContext context: Context,
        apiEndpointProvider: ApiEndpointProvider,
        json: Json,
        @IoScope scope: CoroutineScope,
    ): RemoteConfigRepository {
        val remoteConfig = runCatching { createFirebaseRemoteConfig(context, apiEndpointProvider) }
            .getOrElse {
                Log.w(TAG, "FirebaseRemoteConfig unavailable (${it.message}). Falling back to default values.")
                return DefaultRemoteConfigRepository()
            }
        return RemoteConfigRepositoryImpl(
            remoteConfig = remoteConfig,
            apiEndpointProvider = apiEndpointProvider,
            json = json,
            scope = scope,
        )
    }

    private fun createFirebaseRemoteConfig(
        context: Context,
        apiEndpointProvider: ApiEndpointProvider,
    ): FirebaseRemoteConfig {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val isQaEnv =
            apiEndpointProvider.getApiEndpoint().environmentLevel >= ApiEndpoint.QA.environmentLevel
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds =
                if (isQaEnv) INTERVAL_BY_ONE_MINUTE else INTERVAL_BY_THIRTY_MINUTES
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        // 기본값은 xml 대신 각 RemoteConfigKey.defaultValue 가 담당한다.
        remoteConfig.setCustomSignals(
            customSignals {
                put("isLowEndDevice", LowEndDeviceDetector.isLowEndDevice(context).toString())
            },
        )
        return remoteConfig
    }
}
