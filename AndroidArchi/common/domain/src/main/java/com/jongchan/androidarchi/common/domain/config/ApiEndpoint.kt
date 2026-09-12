package com.jongchan.androidarchi.common.domain.config

/**
 * 앱이 바라보는 서버 환경.
 *
 * `environmentLevel` 이 높을수록 개발자 쪽 환경이다. Remote Config 는 키 뒤에 환경 이름을 붙여
 * (`spotEnableStage`, `spotEnablePublic` ...) 현재 환경 → 하위 환경 순으로 값을 탐색하므로,
 * 상위 환경에서만 값을 덮어쓰고 나머지는 Public 값을 공유할 수 있다.
 *
 * - Custom : 사용자가 직접 입력한 URL
 * - Luke   : 공용 Dev 환경
 * - Stage  : 내부 테스트 서버
 * - Prod   : Public 과 같은 DB, 앱에서는 거의 사용하지 않음
 * - Public : 스토어 빌드 기본 환경
 */
enum class ApiEndpoint(val environmentLevel: Int) {
    Dev(3),
    QA(2),
    Prod(1),
    ;

    /** 현재 환경을 포함해 그보다 낮은(=배포에 가까운) 환경을 높은 레벨부터 나열한다. */
    fun fallbackChain(): List<ApiEndpoint> =
        entries
            .filter { it.environmentLevel <= environmentLevel }
            .sortedByDescending { it.environmentLevel }
}

/** 현재 앱이 사용하는 [ApiEndpoint] 를 알려준다. 구현은 data 레이어(BuildConfig / 개발자 모드 설정)에 둔다. */
interface ApiEndpointProvider {
    fun getApiEndpoint(): ApiEndpoint
}
