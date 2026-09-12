package com.jongchan.androidarchi.common.domain.remoteConfig

import com.jongchan.androidarchi.common.entity.remoteConfig.BottomSheetVO
import com.jongchan.androidarchi.common.entity.remoteConfig.TestAccountVO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Remote Config 키 정의. 키 하나가 (이름, 타입, 기본값) 을 함께 가지므로 호출부에서 캐스팅/기본값 처리가 사라진다.
 *
 * @param key Firebase 콘솔의 키 이름. 실제 조회 시에는 `key + ApiEndpoint.name` (예: `spotEnableStage`) 으로 합성된다.
 * @param serializer 값의 직렬화기. 원시 타입(Boolean/Int/Long/Double/Float/String)은 Firebase 의 typed getter 로,
 *   그 외(List/VO)는 문자열 값을 kotlinx.serialization 으로 파싱한다.
 * @param defaultValue 어느 환경에도 값이 없거나 파싱에 실패했을 때 사용하는 값.
 * @param fetchWhenUpdated 실시간 ConfigUpdate 알림에 이 키가 포함되면 즉시 `fetchAndActivate` 할지 여부.
 *   (`entries` 에 등록된 키만 대상)
 *
 * 새 키 추가: `data object` 를 하나 추가하고, 실시간 갱신이 필요하면 `entries` 에도 넣는다.
 */
sealed class RemoteConfigKey<T>(
    val key: String,
    val serializer: KSerializer<T>,
    val defaultValue: T,
    val fetchWhenUpdated: Boolean = true,
) {
    // ---- Boolean ----
    data object IsShowFeatureA : RemoteConfigKey<Boolean>(
        key = "isShowFeatureA",
        serializer = Boolean.serializer(),
        defaultValue = false,
    )

    // ---- String ----
    data object MarketingPromotionUrl : RemoteConfigKey<String>(
        key = "marketingPromotionUrl",
        serializer = String.serializer(),
        defaultValue = "https://www.aiswmaestro.org/asmMobile/marketingUrl",
    )

    // ---- Int ----
    data object NetworkRetryCount : RemoteConfigKey<Int>(
        key = "networkRetryCount",
        serializer = Int.serializer(),
        defaultValue = 5,
    )

    // ---- List ----
    data object AvailableTabs : RemoteConfigKey<List<String>>(
        key = "availableTabs",
        serializer = ListSerializer(String.serializer()),
        defaultValue = listOf("firstTab", "secondTab", "thirdTab"),
    )

    data object WebSocketReconnectExponentiallyDelayList : RemoteConfigKey<List<Long>>(
        key = "webSocketReconnectExponentiallyDelayList",
        serializer = ListSerializer(Long.serializer()),
        defaultValue = listOf(1000L, 2000L, 4000L, 8000L),
    )

    // ---- JSON object (VO) ----
    data object BottomSheetTooltip : RemoteConfigKey<BottomSheetVO>(
        key = "earnPoolToolTipBottomSheet",
        serializer = BottomSheetVO.serializer(),
        defaultValue = BottomSheetVO(
            title = "Earn Pool",
            description = "A daily reward pool of 2,000 USDT, exclusively for VIPs who have traded over 10M in the last 30 days, distributed based on balance up to 50,000 USDT.",
        ),
    )

    // ---- JSON List (VO) ----
    data object BuiltInTestAccounts : RemoteConfigKey<List<TestAccountVO>>(
        key = "builtInTestAccounts",
        serializer = ListSerializer(TestAccountVO.serializer()),
        defaultValue = emptyList(),
    )

    companion object {
        /**
         * 실시간 ConfigUpdate 알림을 받았을 때 fetch 여부를 판단하는 대상 키.
         * 여기 없는 키는 다음 앱 실행(최초 fetchAndActivate) 또는 minimumFetchInterval 이후에 반영된다.
         */
        val entries: List<RemoteConfigKey<*>> = listOf(
            IsShowFeatureA,
            AvailableTabs,
            MarketingPromotionUrl,
            BottomSheetTooltip,
        )
    }
}
