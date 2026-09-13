package com.jongchan.androidarchi.main.presentation.navigation

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jongchan.androidarchi.main.domain.deeplink.RoutePattern
import com.jongchan.androidarchi.etc.domain.EtcPage
import com.jongchan.androidarchi.etc.presentation.EtcPage
import com.jongchan.androidarchi.etc.presentation.EtcViewModel
import com.jongchan.androidarchi.favorite.domain.FavoritePage
import com.jongchan.androidarchi.favorite.presentation.FavoritePage
import com.jongchan.androidarchi.favorite.presentation.FavoriteViewModel
import com.jongchan.androidarchi.fullScreenMedia.domain.FullScreenMediaPage
import com.jongchan.androidarchi.fullScreenMedia.presentation.FullScreenMediaFragment
import com.jongchan.androidarchi.search.domain.SearchPage
import com.jongchan.androidarchi.search.presentation.SearchPage
import com.jongchan.androidarchi.search.presentation.SearchViewModel
import com.jongchan.androidarchi.intro.domain.IntroPage
import com.jongchan.androidarchi.intro.presentation.IntroPage
import com.jongchan.androidarchi.intro.presentation.IntroViewModel

/**
 * 앱의 모든 페이지 메타데이터 + 렌더러 모음.
 * 새 화면 추가 시 본 리스트에 한 줄을 더한다.
 */
val appRoutes: List<AppRoute> = listOf(
    AppRoute(
        path = IntroPage.PATH,
        render = { IntroPage(viewModel = hiltViewModel<IntroViewModel>()) },
    ),
    AppRoute(
        path = SearchPage.PATH,
        isBottomTab = true,
        render = { SearchPage(viewModel = hiltViewModel<SearchViewModel>()) },
    ),
    AppRoute(
        path = FavoritePage.PATH,
        isBottomTab = true,
        syntheticStack = { args ->
            listOf(
                GenericNavKey(SearchPage.PATH),
                GenericNavKey(FavoritePage.PATH, args),
            )
        },
        render = { FavoritePage(viewModel = hiltViewModel<FavoriteViewModel>()) },
    ),
    AppRoute(
        path = EtcPage.PATH,
        isBottomTab = true,
        syntheticStack = { args ->
            listOf(
                GenericNavKey(SearchPage.PATH),
                GenericNavKey(EtcPage.PATH, args),
            )
        },
        render = { EtcPage(viewModel = hiltViewModel<EtcViewModel>()) },
    ),
    AppRoute(
        path = FullScreenMediaPage.PATH,
        syntheticStack = { args ->
            listOf(
                GenericNavKey(SearchPage.PATH),
                GenericNavKey(FullScreenMediaPage.PATH, args),
            )
        },
        render = { rawArgs ->
            // 상세화면은 XML/ViewBinding Fragment 로 호스팅한다(과제 스펙).
            // 무거운 리스트 대신 origin/url 등 스칼라 인자만 받으므로 디코딩이 실패할 여지는 없고,
            // 빈/유효하지 않은 인자(표시할 미디어 없음)는 ViewModel 이 다이얼로그로 처리한 뒤 뒤로가기 한다.
            val arguments = remember(rawArgs) {
                Bundle().apply { rawArgs.forEach { (key, value) -> putString(key, value) } }
            }
            FragmentHostContainer(
                fragmentClass = FullScreenMediaFragment::class.java,
                arguments = arguments,
                modifier = Modifier.fillMaxSize(),
            )
        },
    ),
)

val appRouteByPath: Map<String, AppRoute> = appRoutes.associateBy { it.path }

val bottomTabRoutes: List<AppRoute> = appRoutes.filter { it.isBottomTab }

/**
 * 동적 구간(`{param}`)을 가진 계층형 라우트의 (패턴, 라우트) 목록.
 *
 * 정적 path 는 [appRouteByPath] 가 O(1) 로 처리하므로, 여기에는 다중 세그먼트 템플릿
 * (예: "/articleList/articlePage/{articleId}")만 보관한다. deep-link URI 해석 시
 * exact 매칭이 실패한 경우에만 이 목록을 순차 매칭한다.
 */
val appRoutePatterns: List<Pair<RoutePattern, AppRoute>> = appRoutes
    .map { route -> RoutePattern(route.path) to route }
    .filter { (pattern, _) -> pattern.hasParams }
