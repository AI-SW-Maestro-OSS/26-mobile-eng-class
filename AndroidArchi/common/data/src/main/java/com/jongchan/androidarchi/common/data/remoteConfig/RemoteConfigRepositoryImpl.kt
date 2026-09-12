package com.jongchan.androidarchi.common.data.remoteConfig

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.jongchan.androidarchi.common.domain.config.ApiEndpoint
import com.jongchan.androidarchi.common.domain.config.ApiEndpointProvider
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigKey
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.Json

/**
 * Firebase Remote Config 기반 [RemoteConfigRepository] 구현. (prex-android `RemoteConfigRepositoryV2Impl` 이식)
 *
 * 동작
 * 1. 생성 시 1회 `fetchAndActivate()`.
 * 2. 실시간 ConfigUpdate 알림에 [RemoteConfigKey.entries] 중 하나라도 포함되면 다시 `fetchAndActivate()`.
 * 3. 조회는 `key + 환경이름` 으로 합성한 raw key 를 현재 환경 → 하위 환경 순으로 탐색한다.
 *    ex) Stage 에서 `spotEnable` 조회 → `spotEnableStage` → `spotEnableProd` → `spotEnablePublic` → defaultValue
 *
 * 원본과의 차이
 * - Gson 대신 kotlinx.serialization([RemoteConfigKey.serializer]) 을 사용한다.
 * - 원본의 `FallbackClient` / `getRecentValue` 경로는 Google Play 정책 대응으로 이미 전부 주석 처리된 죽은 코드라 옮기지 않았다.
 */
class RemoteConfigRepositoryImpl(
    private val remoteConfig: FirebaseRemoteConfig,
    private val apiEndpointProvider: ApiEndpointProvider,
    private val json: Json,
    scope: CoroutineScope,
) : RemoteConfigRepository {

    private val endpoint: ApiEndpoint get() = apiEndpointProvider.getApiEndpoint()

    init {
        scope.launch {
            // 실행 시 최초 1회 fetch
            fetchAndActivateSafely("init")
        }

        remoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    // 원격 업데이트 발생 시, Fetch 가 필요한 경우 Fetch 수행
                    val doFetch = RemoteConfigKey.entries
                        .filter { it.fetchWhenUpdated }
                        .map { it.rawKey(endpoint) }
                        .any { configUpdate.updatedKeys.contains(it) }

                    if (doFetch) {
                        scope.launch { fetchAndActivateSafely("onUpdate") }
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.w(TAG, "addOnConfigUpdateListener.onError code : ${error.code} / cause : ${error.cause}")
                }
            },
        )
    }

    override fun <T> get(key: RemoteConfigKey<T>): T {
        return remoteConfig.getValue(key = key, endpoint = endpoint, json = json).also {
            Log.v(TAG, "Key ${key.key} is fetched from remote config. Value: $it")
        }
    }

    private fun fetchAndActivateSafely(reason: String) {
        // Task 콜백만 붙인다. (kotlinx-coroutines-play-services 의존을 늘리지 않기 위해 await() 는 쓰지 않는다)
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener { activated -> Log.d(TAG, "fetchAndActivate($reason) activated=$activated") }
            .addOnFailureListener { Log.w(TAG, "fetchAndActivate($reason) failed: ${it.message}") }
    }
}

@Suppress("UNCHECKED_CAST", "SwallowedException")
internal fun <T> FirebaseRemoteConfig.getValue(
    key: RemoteConfigKey<T>,
    endpoint: ApiEndpoint,
    json: Json,
): T {
    // 현재 endpoint 보다 낮은 레벨의 환경에 대해서만 값을 가져온다.
    // ex) Custom => Custom, Luke, Sandbox, QA, Stage, Prod, Public
    // ex) Luke   => Luke, Sandbox, QA, Stage, Prod, Public
    // ...
    for (target in endpoint.fallbackChain()) {
        val rawKey = key.rawKey(target)

        if (!getKeysByPrefix(key.key).contains(rawKey)) {
            // 해당 key 가 없을 경우, 다음 endpoint 로 넘어간다.
            Log.v(TAG, "No $rawKey defined on given $endpoint. Try next endpoint.")
            continue
        }

        try {
            val result = when (key.serializer.descriptor.kind) {
                PrimitiveKind.BOOLEAN -> getBoolean(rawKey) as? T
                PrimitiveKind.DOUBLE -> getDouble(rawKey) as? T
                PrimitiveKind.FLOAT -> getDouble(rawKey).toFloat() as? T
                PrimitiveKind.LONG -> getLong(rawKey) as? T
                PrimitiveKind.INT -> getLong(rawKey).toInt() as? T
                PrimitiveKind.STRING -> getString(rawKey) as? T
                else -> {
                    // Primitive 타입이 아닐 경우(List / VO), JSON 문자열을 파싱한다
                    json.decodeFromString(key.serializer, getString(rawKey))
                }
            }
            return result ?: continue
        } catch (e: Exception) {
            Log.v(TAG, "Failed to get value for key $rawKey (${e.message}). Try next endpoint.")
            continue
        }
    }

    Log.w(TAG, "Failed to get value for key ${key.key}. Use default value.")
    return key.defaultValue
}

/** key 이름에 api endpoint 이름을 합성한다. ex) "sampleKey" + "Prod" = "sampleKeyProd" */
internal fun RemoteConfigKey<*>.rawKey(endpoint: ApiEndpoint): String = key + endpoint.name

private const val TAG = "RemoteConfigV2"
