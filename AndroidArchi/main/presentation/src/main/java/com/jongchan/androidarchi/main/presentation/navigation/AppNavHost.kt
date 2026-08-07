package com.jongchan.androidarchi.main.presentation.navigation

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jongchan.androidarchi.common.domain.navigation.NavRoute
import com.jongchan.androidarchi.common.domain.navigation.NavSignal
import com.jongchan.androidarchi.common.presentation.helper.LocalNavigationHelper
import com.jongchan.androidarchi.common.presentation.jank.JankPageEffect
import com.jongchan.androidarchi.intro.domain.IntroPage
import com.jongchan.androidarchi.search.domain.SearchPage

@Composable
fun AppNavHost(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    val navigationHelper = LocalNavigationHelper.current

    LaunchedEffect(Unit) {
        navigationHelper.navigationFlow.collect { signal ->
            when (signal) {
                is NavSignal.GoToDestPage -> handleNavRoute(signal.route, backStack)
                is NavSignal.DeepLink -> handleDeepLink(signal.route, backStack)
                NavSignal.Back -> backStack.removeLastOrNull()
                NavSignal.BackToInitialPage -> navigateToInitialStack(backStack)
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        // 단일 GenericNavKey 디스패처. 실제 화면 결정은 [appRouteByPath] 가 담당한다.
        entryProvider = entryProvider {
            entry<GenericNavKey> { navKey ->
                val route = appRouteByPath[navKey.path]
                if (route == null) {
                    Log.w(TAG, "Unknown path on render: ${navKey.path}")
                    LocalNavigationHelper.current.navigateTo(SearchPage)
                    return@entry
                }
                // 페이지 식별자를 JankStats state 로 등록하고, 백스택 이탈 시 PAGE_EXIT flush.
                JankPageEffect(navKey.path)
                route.render(navKey.args)
            }
        }
    )
}

private const val TAG = "[Navigation]"
private const val INDEX_NOT_FOUND = -1

/**
 * NavRoute 한 건을 받아 백스택에 push(앱 내 전진 이동).
 * Search 으로의 이동은 기존 시맨틱(스택 정리 후 단일 Search 유지)을 그대로 유지한다.
 * 동일 키가 이미 스택에 있으면 중복을 만들지 않고 최전면으로 끌어올린다 — Navigation3 의
 * contentKey 는 키별 1회만 유효하므로, 같은 (path,args) 키가 스택에 둘 이상 존재해선 안 된다.
 * 미등록 path 는 무시 + 경고 로그.
 */
fun handleNavRoute(route: NavRoute, backStack: NavBackStack<NavKey>) {
    if (appRouteByPath[route.path] == null) {
        Log.w(TAG, "Unhandled NavRoute: ${route.path}")
        return
    }
    val navKey = GenericNavKey.of(route)
    if (route.path == SearchPage.PATH) {
        navigateToSearchStack(backStack)
    } else if (backStack.lastOrNull() != navKey) {
        backStack.bringToFront(navKey)
        Log.d(TAG, "navigateTo: $navKey")
    }
}

/**
 * 웜 스타트 deep-link 처리.
 *
 * - 대상이 **bottom-tab 루트**면 콜드/in-app 과 동일한 탭 루트 시맨틱으로 처리한다
 *   ([handleNavRoute] 위임). 탭을 단순 bring-to-front 하면 탭이 루트에서 밀려나
 *   Back 동작/탭 루트가 깨지므로, 이 분기로 세 경로(콜드·웜·in-app)의 탭 백스택 형태를 일치시킨다.
 * - 그 외(leaf 화면)는 **bring-to-front**: 콜드 스타트의 synthetic 부모 체인
 *   ([com.jongchan.androidarchi.main.presentation.deeplink.resolveStartStack])과 달리, 이미 떠 있는
 *   사용자의 스택은 보존하고 대상 키만 최전면으로 올린다(동일 키는 중복 없이 최상단으로).
 *
 * 예) 스택이 `[Search, Favorite]` 인 상태에서 "/articleList/articlePage/123" deep-link 가 도착하면
 *     `[Search, Favorite, ArticlePage(123)]` 이 되어, 기존 맥락을 유지한 채 대상만 전면에 노출된다.
 */
fun handleDeepLink(route: NavRoute, backStack: NavBackStack<NavKey>) {
    val appRoute = appRouteByPath[route.path]
    if (appRoute == null) {
        Log.w(TAG, "Unhandled deep-link route: ${route.path}")
        return
    }
    if (appRoute.isBottomTab) {
        // 루트 탭은 in-app 탭 전환과 동일한 시맨틱으로(탭이 루트에서 밀려나지 않도록).
        handleNavRoute(route, backStack)
        return
    }
    val target = GenericNavKey.of(route)
    if (backStack.lastOrNull() == target) return   // 이미 최전면 — no-op.
    backStack.bringToFront(target)
    Log.d(TAG, "deepLink bringToFront: $target")
}

/**
 * 동일 키(path+args)의 **모든** 출현을 제거한 뒤 최상단에 추가한다.
 * 백스택에 같은 키가 둘 이상 남지 않도록 보장한다(Navigation3 의 contentKey 중복 방지).
 * MutableList.remove 가 첫 출현만 지우는 것과 달리 removeAll 로 잔존 중복까지 제거한다.
 */
private fun NavBackStack<NavKey>.bringToFront(key: NavKey) {
    removeAll { it == key }
    add(key)
}

/**
 * 세션 만료 처리.
 *
 * 스택을 비우고 Intro 단독으로 교체한다. 인증이 필요한 화면들이
 * 백스택에 남아 있으면 뒤로가기 했을 때 만료된 세션 상태로 다시 노출되기 때문이다.
 */
private fun navigateToInitialStack(backStack: NavBackStack<NavKey>) {
    backStack.clear()
    backStack.add(GenericNavKey(IntroPage.PATH))
    Log.d(TAG, "backToInitial: reset stack to Intro")
}

private fun navigateToSearchStack(backStack: NavBackStack<NavKey>) {
    val searchIndex = backStack.indexOfFirst { it is GenericNavKey && it.path == SearchPage.PATH }
    if (searchIndex == INDEX_NOT_FOUND) {
        backStack.clear()
        backStack.add(GenericNavKey(SearchPage.PATH))
        Log.d(TAG, "navigateTo: Search (fresh stack)")
    } else {
        while (backStack.size > searchIndex + 1) {
            backStack.removeAt(backStack.lastIndex)
        }
        Log.d(TAG, "BackNavigate To: Search")
    }
}
