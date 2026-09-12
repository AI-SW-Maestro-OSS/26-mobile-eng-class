---
name: navigation-conventions
description: How this project's Navigation3 model works and the invariants every screen / route / deep link must follow — NavRoute / Page objects, GenericNavKey, AppRouteRegistry, syntheticStack, the deep-link cold/warm policy, RoutePattern templates (nested paths), and NavigationHelper. Trigger when adding or changing a screen, route, deep link, back-stack behavior, AppRouteRegistry entry, Page navigation object, or NavigationHelper usage; or when reasoning about why a back stack / deep link behaves a certain way. Pairs with `make-new-feature-module` (scaffolding) and the canonical [docs/architecture/navigation.md](docs/architecture/navigation.md).
---

# Navigation conventions (Navigation3)

This project uses **Jetpack Navigation3**. The core idea: **the app owns the back stack** (a `NavBackStack<NavKey>`), and a single host renders it. Navigation3 deliberately has **no built-in deep-link parser / NavController** — routing and deep-link → back-stack mapping are app code, centralized here.

Canonical deep dive: [docs/architecture/navigation.md](docs/architecture/navigation.md). This skill is the **enforcement layer** — the rules to follow when touching navigation, and the shapes a route can take. For scaffolding a brand-new feature, use `make-new-feature-module` (its §2.8 / §5 hold the copy-paste templates).

## The model — one key type, path-based dispatch

| Layer | Type / file | Role |
|---|---|---|
| domain (pure JVM) | `NavRoute(path, args: Map<String,String>)` | The single navigation unit. `args` are **all String**. |
| domain | `interface Page { fun toRoute(): NavRoute }` | Each screen's typed entry point (in `<feature>/domain`). |
| domain | `sealed interface NavSignal { GoToDestPage / DeepLink / Back / BackTo / BackToInitialPage }` | What flows through the nav channel. |
| domain | `interface NavigationHelper` | `navigateTo(Page)` (recommended) / `navigateByRoute` / `navigateDeepLink` / `navigateToBack` / `navigateBackTo(Page)` (pop everything above an existing page; path-matched, no-op if absent) / `navigateToInitial`. The only thing domain knows. |
| **main/domain** (pure JVM) | `deeplink/RoutePattern.kt`, `deeplink/RouteMatcher.kt` (`matchRoute`) | Pure route matching — template `{param}` extraction + exact/template resolution. **Unit-tested here** (project convention: tests live in `domain`). |
| host | `GenericNavKey(@Serializable path, args)` : `NavKey` | **The one and only back-stack entry type.** Every destination is this; the screen is chosen by `path`. |
| host | `AppRoute(path, isBottomTab, syntheticStack, render)` | Per-route host metadata. |
| host | `AppRouteRegistry.kt` | **Single registration point** — `appRoutes` list + derived `appRouteByPath`, `appRoutePatterns`, `bottomTabRoutes`. |
| host | `AppNavHost.kt` | Collects `navigationFlow`, mutates the back stack, renders `NavDisplay` + `entry<GenericNavKey>`. |
| host | `deeplink/NavRouteUriParser.kt` | `Uri.resolveRoute()` → injects the registry into `matchRoute`; cold/warm deep-link entry points. |

A screen's identity is its **`path`**. The same path drives in-app navigation, the back-stack key, render dispatch (`appRouteByPath[path]`), and the deep-link URL — that's why "adding a screen gives you its deep link for free."

## Golden rules (invariants — violating these breaks routing or crashes Compose)

1. **`Page.PATH` is the canonical identity.** It is the in-app path, the `GenericNavKey.path`, the render-dispatch key, and the deep-link template — all the same string. Register every path in `AppRouteRegistry.appRoutes`; nowhere else.
2. **For nested/parameterized routes, `PATH` is the TEMPLATE string** (e.g. `"/articleList/articlePage/{articleId}"`). **Never** splice the concrete value into the path — the value goes in `args` only. (The key stored in the back stack literally contains `{articleId}`; the value `123` lives in `args`.) This keeps `appRouteByPath[path]` O(1), keeps serialization/process-death restore safe, and lets `RoutePattern` extract the value.
3. **Template param name == Args key == `Args.from` lookup key**, char-for-char. `{articleId}` ↔ `KEY_ARTICLE_ID = "articleId"` ↔ `args["articleId"]`. Mismatch silently drops the value.
4. **Cold-start back stack is defined ONLY in `AppRoute.syntheticStack`** (single source of truth). The warm/deep-link path does not redefine per-route stacks.
5. **No duplicate keys in the back stack.** Navigation3's `contentKey` defaults to `key.toString()`, which must be unique per back-stack entry. Use the `bringToFront` helper (`removeAll { it == key }; add(key)`) when re-surfacing an existing key — never a bare `add` that can duplicate, and never `remove` (removes only the first occurrence).
6. **In-app navigation goes through `navigationHelper.navigateTo(Page)`** (CLAUDE.md rule 7). Construct the `Page`/`Args` and call `navigateTo`; don't poke the back stack from a screen.
7. **`args` are String-only.** Complex types serialize to a JSON string (`NavRouteJson`) — see `FullScreenMediaPage` for the typed-Args golden example.

## In-app navigation vs deep links — two separate signals

- **In-app** (tab click, list-item click): `navigateTo(Page)` → `NavSignal.GoToDestPage` → `handleNavRoute` → **push** (with `bringToFront` dedup). Navigating to `Search` (the home tab) resets to a single `Search` root (`navigateToSearchStack`).
- **Back to a specific page**: `navigateBackTo(Page)` → `NavSignal.BackTo` → `popBackTo` → pops every entry above the **topmost** entry whose `path` matches, so that page is on top again (`[Search, A, B, C]` + `BackTo(A)` → `[Search, A]`). Match is by `path` only (args ignored). If the page is not in the stack it is a **no-op with a warning** — it never pushes; use `navigateTo` for forward moves.
- **Deep link, cold start** (`MainActivity.onCreate`): `resolveStartStack(intent.data)` → matched route's `syntheticStack` lays the **full parent chain**; unmatched → Intro fallback. Built before `rememberNavBackStack`.
- **Deep link, warm start** (`MainActivity.onNewIntent`, `singleTop`): `resolveNewIntentRoute` → `navigateDeepLink` → `NavSignal.DeepLink` → `handleDeepLink`:
  - **leaf screen** → **bring-to-front**: keep the user's current stack, surface only the target key. (Preserving context is intended.)
  - **tab root** (`isBottomTab` — flag is named `isBottomTab`, but it renders as a **TOP** tab bar via `RootComposable`'s `Scaffold(topBar = { TopTabBar(...) })`) → delegate to `handleNavRoute` (tab-root semantics), so a tab is never demoted from the root and Back stays correct.

> ⚠️ **Illustrative example.** `/articleList/articlePage/{articleId}` (and the `123` rows below) is a **hypothetical** route used to show the nested/`{param}` mechanism. There is currently **no dynamic `{param}` route registered** in `appRoutes` (so `appRoutePatterns` is empty at runtime); the only registered paths are `""` (Intro), `/search`, `/favorite`, `/fullScreenMedia`. The example is validated only by the pure-logic unit tests (`RouteMatcherTest`/`RoutePatternTest`), which use a **fake** registry.

| Entry | Back stack for `/articleList/articlePage/123` (current `[Search, Favorite]`) |
|---|---|
| Cold start | `[Home, ArticleList, ArticlePage(123)]` (full `syntheticStack`) |
| Warm, leaf | `[Search, Favorite, ArticlePage(123)]` (context preserved) |
| Warm, tab path | in-app tab semantics (tab stays root) |

## Deep-link resolution (RoutePattern)

`Uri.resolveRoute()` (host) extracts segments + query, then delegates to the pure `matchRoute(segments, query, literalPaths, templates)` in **`main/domain`**:
1. **Exact literal match** first (`/search`, `/fullScreenMedia`).
2. **Template match** (`RoutePattern`) for paths with `{param}` — extracts path-segment values into `args`. Path params win over query params on name collision.

A route whose `PATH` contains `{` is **auto-added to `appRoutePatterns`** (via `hasParams`) — no extra registration. `RoutePattern` / `matchRoute` are pure (Android-free) and live in `main/domain`, unit-tested there ([RoutePatternTest](main/domain/src/test/java/com/jongchan/androidarchi/main/domain/deeplink/RoutePatternTest.kt), [RouteMatcherTest](main/domain/src/test/java/com/jongchan/androidarchi/main/domain/deeplink/RouteMatcherTest.kt)) — add cases there when you add a template shape.

## Adding a route — pick the shape

All four shapes are scaffolded by `make-new-feature-module` (Page object → its §2.8; registry entry → its §5). Summary:

| Shape | `Page` | Registry `AppRoute` |
|---|---|---|
| **Simple** (intro/search) | `object XPage : Page { const val PATH = "/x" }` | `path`, `render` |
| **Tab** (search/favorite) — flag `isBottomTab`, rendered as a **TOP** tab bar (`TopTabBar`) | same | `+ isBottomTab = true`, `syntheticStack = [Search, X]` |
| **Typed Args** (fullScreenMedia) | `object XPage { const val PATH = "/x"; data class Args(...) : Page { toRoute()/from() } }` | `syntheticStack = [Search, X(args)]`, `render` decodes `Args.from` |
| **Nested / `{param}`** (articleList → articlePage — *illustrative; not currently registered*) | `PATH = "/parent/x/{id}"`, `Args(id)` (rules 2–3) | `syntheticStack = [Home, Parent, X(args)]`; template auto-registers for deep links |

A single feature module may host **multiple screens** (e.g. a list + its detail) — that's just multiple `Page` objects + multiple Composables + multiple registry entries. The parent referenced in a nested `syntheticStack` must itself be a registered route.

## DO / DON'T

- **DO** register every new path in `AppRouteRegistry.appRoutes`, and add the `:<feature>:domain` + `:<feature>:presentation` deps to `:main:presentation` so the host can import the `Page`/Composable.
- **DO** define the cold stack only in `syntheticStack`; root nested stacks at `Search` (the home tab), mirroring `favorite`/`fullScreenMedia`.
- **DO** use `navigateTo(Page)` for in-app moves and typed `Args` for parameters.
- **DON'T** put a path-param value into `PATH` — `PATH` stays the `{param}` template; values go in `args`.
- **DON'T** let the param name, `KEY_*`, and `Args.from` key drift apart.
- **DON'T** `add` a key that may already be in the stack, or use `remove` (first-only) to dedupe — use `bringToFront`.
- **DON'T** redefine a route's cold back stack outside `syntheticStack`, and don't handle deep links via a `NavController` or Nav3 built-in (there is none — it's `RoutePattern` + the registry).
- **DON'T** put the navigation `Page` object in `presentation` — it lives in `<feature>/domain`.

## Canonical references

- Contracts: [NavRoute.kt](common/domain/src/main/java/com/jongchan/androidarchi/common/domain/navigation/NavRoute.kt), [NavSignal.kt](common/domain/src/main/java/com/jongchan/androidarchi/common/domain/navigation/NavSignal.kt), [NavigationHelper.kt](common/domain/src/main/java/com/jongchan/androidarchi/common/domain/helper/NavigationHelper.kt)
- Host: [AppRouteRegistry.kt](main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppRouteRegistry.kt), [AppRoute.kt](main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppRoute.kt), [AppNavHost.kt](main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/AppNavHost.kt), [GenericNavKey.kt](main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/navigation/GenericNavKey.kt)
- Deep link — pure matching (main/domain): [RoutePattern.kt](main/domain/src/main/java/com/jongchan/androidarchi/main/domain/deeplink/RoutePattern.kt), [RouteMatcher.kt](main/domain/src/main/java/com/jongchan/androidarchi/main/domain/deeplink/RouteMatcher.kt). Host glue (main/presentation): [NavRouteUriParser.kt](main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/deeplink/NavRouteUriParser.kt), [MainActivity.kt](main/presentation/src/main/java/com/jongchan/androidarchi/main/presentation/MainActivity.kt)
- Full prose + diagrams: [docs/architecture/navigation.md](docs/architecture/navigation.md)
- Scaffolding a new feature/screen: `make-new-feature-module`

Manual smoke test: `adb shell 'am start -W -a android.intent.action.VIEW -d "https://www.androidarchi.com/<path>" com.jongchan.androidarchi'`
