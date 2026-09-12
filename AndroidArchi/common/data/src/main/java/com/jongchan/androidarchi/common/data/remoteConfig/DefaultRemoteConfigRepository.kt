package com.jongchan.androidarchi.common.data.remoteConfig

import android.util.Log
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigKey
import com.jongchan.androidarchi.common.domain.remoteConfig.RemoteConfigRepository

/**
 * Firebase 가 초기화되지 않은 환경(app/google-services.json 미배치)에서 쓰는 대체 구현.
 * 모든 키에 대해 [RemoteConfigKey.defaultValue] 를 돌려주므로 호출부는 Firebase 유무를 몰라도 된다.
 */
class DefaultRemoteConfigRepository : RemoteConfigRepository {
    override fun <T> get(key: RemoteConfigKey<T>): T {
        Log.v(TAG, "Firebase not initialized. Key ${key.key} uses default value: ${key.defaultValue}")
        return key.defaultValue
    }

    private companion object {
        const val TAG = "RemoteConfigV2"
    }
}
