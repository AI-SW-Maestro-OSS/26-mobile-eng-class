package com.jongchan.androidarchi.common.domain.helper

import com.jongchan.androidarchi.common.domain.navigation.NavRoute
import com.jongchan.androidarchi.common.domain.navigation.NavSignal
import com.jongchan.androidarchi.common.domain.navigation.Page
import kotlinx.coroutines.flow.Flow

/**
 * 단일 네비게이션 플로우. 전진/후진 모두 [NavSignal] 한 가지 형식으로 emit 된다.
 *
 * 호출부는 다음 중 하나로 사용한다.
 * - [navigateByRoute] — 직접 [NavRoute] 를 구성해서 전진 이동(앱 내 push).
 * - [navigateTo] — 각 feature 가 정의한 [Page] 객체를 그대로 전달 (권장).
 * - [navigateDeepLink] — 앱 실행 중 도착한 deep-link 를 호스트로 전달(웜 스타트 bring-to-front 정책).
 * - [navigateToBack] — 하드웨어 백 키와 동일하게 한 단계 뒤로 이동.
 * - [navigateBackTo] / [navigateBackToRoute] — 백스택에 이미 있는 특정 [Page] 까지 되돌아간다.
 *   그 페이지를 덮고 있는 상위 엔트리를 모두 pop 하며, 스택에 없으면 no-op (push 하지 않음).
 * - [navigateToInitial] — 세션 만료 등으로 인한 초기화면 이동 Signal을 전달한다.
 */
interface NavigationHelper {
    val navigationFlow: Flow<NavSignal>
    fun navigateByRoute(route: NavRoute)
    fun navigateTo(page: Page)
    fun navigateDeepLink(route: NavRoute)
    fun navigateToBack()
    fun navigateBackTo(page: Page)
    fun navigateBackToRoute(route: NavRoute)
    fun navigateToInitial()
}
