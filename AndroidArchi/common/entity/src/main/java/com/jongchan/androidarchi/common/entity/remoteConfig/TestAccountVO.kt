package com.jongchan.androidarchi.common.entity.remoteConfig

import kotlinx.serialization.Serializable

/**
 * 디버그 빌드에서 Remote Config 로 내려받는 내장 테스트 계정.
 * `RemoteConfigKey.BuiltInTestAccounts` (@DebugOnlyRemoteConfigApi) 로만 접근한다.
 */
@Serializable
data class TestAccountVO(
    val id: String = "",
    val pwd: String = "",
) {
    companion object {
        val empty = TestAccountVO()
    }
}
