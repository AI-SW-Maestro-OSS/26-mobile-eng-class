package com.jongchan.androidarchi.common.domain.remoteConfig

/**
 * 타입 안전한 Remote Config 조회.
 *
 * ```
 * val enabled: Boolean = remoteConfigRepositoryV2.get(RemoteConfigKey.SpotEnable)
 * val tabs: List<String> = remoteConfigRepositoryV2.get(RemoteConfigKey.ChallengeTabOrder)
 * val sheet: BottomSheetVO = remoteConfigRepositoryV2.get(RemoteConfigKey.EarnPoolToolTipBottomSheet)
 * ```
 *
 * 값을 얻지 못하면(키 미정의, 파싱 실패, Firebase 미초기화) 항상 [RemoteConfigKey.defaultValue] 를 반환한다.
 * 원본(prex-android)의 `getList(key, Class<T>)` 는 Gson 의 타입 소거 때문에 필요했던 API 로,
 * 여기서는 [RemoteConfigKey.serializer] 가 원소 타입까지 담으므로 [get] 하나로 대체된다.
 */
interface RemoteConfigRepository {
    fun <T> get(key: RemoteConfigKey<T>): T
}
