package com.jongchan.androidarchi.common.data.config

import com.jongchan.androidarchi.common.data.BuildConfig
import com.jongchan.androidarchi.common.domain.config.ApiEndpoint
import com.jongchan.androidarchi.common.domain.config.ApiEndpointProvider

/**
 * 빌드 타입으로 환경을 결정하는 기본 구현.
 *
 * API-CONFIG-INJECTION-POINT: 개발자 모드에서 환경을 바꾸는 기능이 생기면 여기서 저장된 값을 읽도록 교체한다.
 * (prex-android 의 `DeviceHelper.getApiEndpoint()` 에 해당)
 */
class ApiEndpointProviderImpl : ApiEndpointProvider {
    override fun getApiEndpoint(): ApiEndpoint =
        if (BuildConfig.DEBUG) ApiEndpoint.Dev else ApiEndpoint.Prod
}
