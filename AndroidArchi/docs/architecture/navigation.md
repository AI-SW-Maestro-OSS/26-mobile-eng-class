# 네비게이션 (Navigation3)

## 왜

- **앱 내 이동과 딥링크가 같은 path를 공유**한다 — `/search` 는 탭 이동에서도, `https://www.androidarchi.com/search` 딥링크에서도 동일하게 동작. 화면 추가 시 딥링크가 공짜로 따라온다.
- domain 레이어는 `NavigationHelper` 인터페이스만 알고 Navigation3를 모른다 — UseCase가 화면 이동을 트리거해도 플랫폼 의존이 생기지 않는다.

## 계약 (common/domain — 순수 JVM)

골든 예제: [NavRoute.kt](../../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/navigation/NavRoute.kt), [NavSignal.kt](../../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/navigation/NavSignal.kt), [NavigationHelper.kt](../../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/helper/NavigationHelper.kt)

```kotlin
data class NavRoute(val path: String, val args: Map<String, String> = emptyMap())
interface Page { fun toRoute(): NavRoute }            // feature/domain이 구현
sealed interface NavSignal { GoToDestPage(route) / DeepLink(route) / Back / BackTo(route) / BackToInitialPage }
interface NavigationHelper {
    val navigationFlow: Flow<NavSignal>
    fun navigateTo(page: Page)            // 권장 진입점 (앱 내 push)
    fun navigateByRoute(route: NavRoute)  // NavRoute 직접 구성해서 push
    fun navigateDeepLink(route: NavRoute) // 웜 딥링크 — bring-to-front (탭 대상은 탭 루트 시맨틱)
    fun navigateToBack()
    fun navigateBackTo(page: Page)         // 스택에 이미 있는 Page 까지 pop (덮고 있는 엔트리 전부 제거, 없으면 no-op)
    fun navigateBackToRoute(route: NavRoute)
    fun navigateToInitial()               // 세션 만료 — 스택 비우고 Intro 단독
}
```

- **`navigateBackTo(page)`** (`NavSignal.BackTo` → `popBackTo`): 대상 `path` 가 최전면에 올 때까지 위쪽 엔트리를 모두 pop 한다. 매칭은 path 기준이라 같은 path 가 args 만 다르게 여러 개면 가장 최근 것까지만 되돌아간다. 스택에 없으면 push 하지 않고 경고 로그만 남긴다(전진은 `navigateTo`). 예) `[Search, A, B, C]` + `navigateBackTo(A)` → `[Search, A]`.

- args는 전부 String — 복잡 타입은 `NavRouteJson`으로 JSON 직렬화 (typed Args 골든 예제: [FullScreenMediaPage.kt](../../fullScreenMedia/domain/src/main/java/com/jongchan/androidarchi/fullScreenMedia/domain/FullScreenMediaPage.kt))
- Page 객체는 **feature/domain** 모듈에 둔다: `object SearchPage : Page { const val PATH = "/search" }`

## 호스트 (main/presentation)

| 파일 | 역할 |
|---|---|
| [AppRoute.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppRoute.kt) | path / isBottomTab / syntheticStack / render(Composable) |
| [AppRouteRegistry.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppRouteRegistry.kt) | **모든 라우트의 단일 등록처** — 새 화면은 여기에 항목 추가 |
| [AppNavHost.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppNavHost.kt) | `navigationFlow` collect → 백스택 조작, `NavDisplay` + `entry<GenericNavKey>` 렌더 |
| [GenericNavKey.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/GenericNavKey.kt) | `@Serializable (path, args)` — 백스택 엔트리 단일 타입 |
| [RootComposable.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/RootComposable.kt) | Scaffold + 탭바(`isBottomTab` 라우트) + SnackbarHost. 플래그 이름은 `isBottomTab` 이지만 실제 UI 는 **상단** 탭바(`topBar = { TopTabBar(...) }`)로 렌더된다 |

새 화면 등록 예 (args 없는 기본형):

```kotlin
AppRoute(
    path = MyFeaturePage.PATH,
    render = { MyFeaturePage(viewModel = hiltViewModel<MyFeatureViewModel>()) },
),
```

## Synthetic backstack (콜드 스타트 단일 출처)

딥링크/탭으로 "중간 화면"에 바로 진입해도 백 키가 자연스럽도록, 라우트가 자기 진입 시 스택 전체를 합성한다 (favorite 골든 예제). **콜드 스타트 백스택은 오직 `syntheticStack` 한 곳에서만 정의된다** — 웜 스타트(아래)는 이 스택을 재정의하지 않고 별도 정책으로 동작한다.

```kotlin
syntheticStack = { args -> listOf(
    GenericNavKey(SearchPage.PATH),          // 뒤로가기 목적지를 먼저 깔고
    GenericNavKey(FavoritePage.PATH, args),  // 자기 자신
) },
```

계층형 path도 동일하게 표현한다 — `[Home, ArticleList, ArticlePage]`:

> ⚠️ **예시(가상)**: 아래 `/articleList/articlePage/{articleId}` 라우트는 중첩/동적 라우트 메커니즘을 보여주기 위한 **설명용 예제**다. 현재 `appRoutes` 에는 `{param}` 동적 라우트가 등록되어 있지 않으므로(`appRoutePatterns` 는 런타임에 비어 있음), 이 예제는 fake 레지스트리를 쓰는 단위 테스트(`RouteMatcherTest`/`RoutePatternTest`)로만 검증된다. 실제 등록 라우트는 `""`(Intro)·`/search`·`/favorite`·`/fullScreenMedia` 뿐이다.

```kotlin
AppRoute(
    path = ArticlePage.PATH,   // "/articleList/articlePage/{articleId}"
    syntheticStack = { args -> listOf(
        GenericNavKey(IntroPage.PATH),                  // Home
        GenericNavKey(ArticleListPage.PATH),            // 부모 리스트
        GenericNavKey(ArticlePage.PATH, args),          // 자기 자신(articleId 포함)
    ) },
    render = { args -> ArticlePage(ArticlePage.Args.from(args)) },
)
```

## 딥링크 (App Links)

- 매니페스트: `https://www.androidarchi.com` autoVerify intent-filter ([AndroidManifest.xml](../../app/src/main/AndroidManifest.xml))
- [NavRouteUriParser.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/deeplink/NavRouteUriParser.kt) `Uri.resolveRoute()`: URI를 등록 라우트로 해석한다.
  1. **정적 path exact 매칭** (`/search`, `/fullScreenMedia`) — 항상 우선.
  2. **동적 템플릿 매칭** ([RoutePattern.kt](../../main/domain/src/main/java/com/jongchan/androidarchi/main/domain/deeplink/RoutePattern.kt)) — `{param}` 구간을 가진 다중 세그먼트 path(`/articleList/articlePage/{articleId}`)를 매칭하고 path 구간 값(articleId)을 args로 추출. path 파라미터는 query 와 충돌 시 우선.
  - 순수 매칭 로직(`RoutePattern` / `matchRoute`)은 **`main/domain`**(kotlin-jvm)에 있고 단위 테스트도 거기 둔다. `main/presentation`의 `Uri.resolveRoute()`는 레지스트리(`appRouteByPath`/`appRoutePatterns`)를 주입만 한다.
  - 해석된 `NavRoute.path`는 항상 **템플릿 path**(= `GenericNavKey` 식별자, in-app path와 동일). 동적 값은 args에 들어가므로 렌더 dispatch(`appRouteByPath[path]`)와 직렬화/복원은 그대로 O(1)·안전하게 동작.
- **콜드 스타트** `resolveStartStack`: 매칭 라우트의 `syntheticStack` 전체를 깐다 → 부모 체인 포함. 미매칭 → 인트로 폴백.
- **웜 스타트** `resolveNewIntentRoute` → `navigateDeepLink(route)` → `NavSignal.DeepLink` → [handleDeepLink](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppNavHost.kt):
  - **leaf 화면**: **bring-to-front** — 기존 스택은 보존하고 대상 키(path+args)만 최전면으로(동일 키는 `removeAll` 후 최상단 → 중복 키 금지). 사용자의 현재 맥락 유지가 의도된 동작.
  - **탭 루트**(`isBottomTab` — 플래그명은 bottom 이지만 실제로는 상단 `TopTabBar`): 단순 bring-to-front 하면 탭이 루트에서 밀려 Back 동작이 깨지므로, 콜드/in-app 과 동일한 **탭 루트 시맨틱**(`handleNavRoute` 위임)으로 처리.
  - in-app 전진 이동(`GoToDestPage`, push)과는 신호 자체가 분리되어 서로 영향을 주지 않는다.

다음 표의 `/articleList/articlePage/123` 은 위 가상 예제와 동일하게 **설명용**이다(현재 앱에 등록된 동적 라우트는 없음 — 동작 원리만 보여준다):

| 진입 | 처리 | 예: `/articleList/articlePage/123`, 현재 `[Search, Favorite]` |
|---|---|---|
| 콜드 스타트 | `syntheticStack` 전체 | `[Home, ArticleList, ArticlePage(123)]` |
| 웜 스타트 (leaf) | bring-to-front | `[Search, Favorite, ArticlePage(123)]` (기존 스택 보존) |
| 웜 스타트 (탭 path) | 탭 루트 시맨틱 | in-app 탭 전환과 동일 (탭이 루트에서 밀려나지 않음) |

- 테스트: `adb shell 'am start -W -a android.intent.action.VIEW -d "https://www.androidarchi.com/<path>" com.jongchan.androidarchi'` (`<path>` 는 실제 등록 라우트 — `search`/`favorite`/`fullScreenMedia` — 로 대체. 위 `articleList/...` 는 가상 예제라 그대로는 해석되지 않음)

## Fragment 혼합 호스팅 (fullScreenMedia 골든 예제)

ViewBinding Fragment가 필요한 화면(비디오 플레이어 등)은 [FragmentHostContainer.kt](../../main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/FragmentHostContainer.kt)로 Compose 라우트 안에 호스팅한다 — 라우트 render에서 args를 Bundle로 변환해 전달. 백스택/딥링크는 Compose 화면과 완전히 동일하게 동작한다.
