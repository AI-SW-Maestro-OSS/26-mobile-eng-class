# 성능 계측 (TTI / JankStats / Baseline Profile)

## 왜

- 화면이 늘어날 때마다 "이 페이지 왜 느리지"를 측정 없이 추측하지 않도록, **페이지 단위 계측을 아키텍처에 내장**한다.
- 새 feature를 만들 때 TTI/Jank 계측을 따라 붙이는 것이 컨벤션이다 (필수는 페이지 단위 Jank — AppNavHost가 자동 적용, TTI는 핵심 화면에 선택 적용).

## 1. TTI (Time To Initial Display)

순수 JVM 모듈 `:tti`. 인터페이스: [TTIHelper.kt](../../tti/src/main/java/com/jongchan/androidarchi/tti/TTIHelper.kt)

`TTIHelper` 는 **`@ViewModelScoped`** 로 바인딩되어 ViewModel 인스턴스당 1개 생성되고, 같은 ViewModel 에 주입되는 UseCase 들과 동일 인스턴스를 공유한다. reporter / logger / `@TtiDispatcher` 는 Hilt 싱글톤을 주입받고, 프로세스 전역이어야 하는 페이지별 인스턴스 번호 카운터만 `TTIHelperImpl` 의 companion 에 둔다.

```kotlin
interface TTIHelper {                    // 한 번의 TTI 측정(화면 인스턴스 1개)에 대한 핸들
    fun startTTITracking(page: TTIPage)   // ViewModel init 에서 1회 호출 (중복 호출 시 예외)
    fun startTTITimeline(category)        // 구간 시작 (VIEW_CREATION / API_REQUEST / IMAGE_LOADED ...)
    fun endTTITimeline(category)          // 구간 끝
    fun endTTITracking()                  // 최초 의미있는 렌더 완료
    fun shotTTILogging()                  // 페이지 이탈 시 로깅 발사
    fun addTTIMetaData(metadata, value)
}
```

`startTTITracking` 전에 들어온 호출은 무시된다 — 트래킹을 시작하지 않는 ViewModel 아래의 UseCase 가 마크를 찍어도 안전하다.

**멀티 인스턴스**: 같은 페이지를 동시에 여러 곳에 띄워도(분할 뷰, 듀얼 pane, backstack 중복) 인스턴스별로 독립 측정된다. 인스턴스 식별은 ViewModel 별 TTIHelper 인스턴스가 담당한다. TTI 이벤트는 Hilt 의 `@TtiDispatcher`(= `@IoDispatcher` 의 `limitedParallelism(1)` 뷰, [CoroutineModule](../../common/presentation/src/main/java/com/jongchan/androidarchi/common/presentation/coroutine/CoroutineModule.kt)) 직렬 레인에서 제출 순서대로 실행되어 한 TTI 과정 내 마크 순서가 보장되고, 인스턴스별 `SupervisorJob` 이 수명(shot/timeout 후 cancel)을 관리한다. 로그 키는 `"{pageName}#{instanceNo}_{millis}"` — `instance_no` 는 페이지별로 단조 증가한다(프로세스 재시작 시 1부터).

적용 절차 (골든 예제: [FullScreenMediaFragment.kt](../../fullScreenMedia/presentation/src/main/java/com/jongchan/androidarchi/fullScreenMedia/presentation/FullScreenMediaFragment.kt)):

1. feature/domain에 `object {Feature}TTIPage : TTIPage` 정의 ([FullScreenMediaTTIPage.kt](../../fullScreenMedia/domain/src/main/java/com/jongchan/androidarchi/fullScreenMedia/domain/tti/FullScreenMediaTTIPage.kt))
2. ViewModel 생성자로 `val ttiHelper: TTIHelper` 를 주입받고, `init` 에서 다른 작업(bootstrap)보다 먼저 `ttiHelper.startTTITracking({Feature}TTIPage)` 를 호출해 이후의 모든 마크보다 트래킹 시작이 먼저 오도록 보장한다 ([FullScreenMediaViewModel.kt](../../fullScreenMedia/presentation/src/main/java/com/jongchan/androidarchi/fullScreenMedia/presentation/FullScreenMediaViewModel.kt)).
   `{Feature}TTIPage.timelines` 에는 **실제로 측정하는 구간만** 넣는다. 마지막 타임라인이 완성되어야 `endTTITracking()` 이 받아들여지므로, 찍지 않는 구간(예: API 가 없는 화면의 `API_RESPONSE_TIME`)을 넣으면 `endTTITracking()` 이 무시되어 매번 미완료(타임아웃 또는 이탈 시 `is_bounced`)로 리포트된다. 골든 예제는 실제 API 는 없지만 UseCase 에서 API 구간을 찍는 패턴을 보여주기 위해 데이터 로드를 API 호출처럼 감싸 API 구간까지 측정한다.
3. View(Fragment/Composable)는 `viewModel.ttiHelper` 로 구간별 `start/endTTITimeline` (뷰 생성 → 바인딩 → 이미지 로드)을 찍고, 핵심 콘텐츠 표시 시 `endTTITracking()`, 이탈 시(`onDestroyView` 등) `shotTTILogging()` 호출.
4. `BaseUseCase`도 같은 `ttiHelper`(@ViewModelScoped)를 주입받으므로 UseCase 안에서도 `ttiHelper.startTTITimeline(...)` 으로 마크를 찍을 수 있다. API 구간(`API_REQUEST_READY_TIME` / `API_RESPONSE_TIME`)이 있는 화면이라면 suspend 호출을 같은 코루틴 안에서 start/end 로 감싼다 ([GetFullScreenMediaItemsUseCase.kt](../../fullScreenMedia/domain/src/main/java/com/jongchan/androidarchi/fullScreenMedia/domain/GetFullScreenMediaItemsUseCase.kt)).

> 현재 TTI가 실제로 연결된 feature는 `fullScreenMedia` 하나뿐이다 (유일한 `TTIPage` 구현체 = `FullScreenMediaTTIPage`). `search` 는 `SearchUseCase`가 `ttiHelper`를 `BaseUseCase`로 넘기기만 할 뿐 TTI를 측정하지 않는다. (`SearchTTIPage` 는 [TTIPage.kt](../../tti/src/main/java/com/jongchan/androidarchi/tti/TTIPage.kt) 주석의 *예시*일 뿐 실제 구현체가 아니다.)

Logcat 출력 예: `Shot TTI Logging: ... tti.tti_time=..., tti.view_creation_time=..., tti.api_request_ready_time=..., tti.api_response_time=..., tti.view_binding_time=..., tti.image_loaded_time=...`

기록 필드 ([TTIHelperImpl.kt](../../tti/src/main/java/com/jongchan/androidarchi/tti/TTIHelperImpl.kt) / [TTIEnums.kt](../../tti/src/main/java/com/jongchan/androidarchi/tti/TTIEnums.kt)): `page_name`, `instance_no`, `tti_time`, `api_request_ready_time`, `api_response_time`, `view_creation_time`, `view_binding_time`, `image_loaded_time`, `is_bounced`, `tti_log_version`. `endTTITracking` 이 20초(`TTI_TIMEOUT_MILLISECONDS`) 안에 도착하지 않으면 워치독이 "Timeout TTI Tracking" 리포트를 자동 발사한다. 리포트는 인스턴스당 정확히 1회(`shotTTILogging`/타임아웃 중 선착)이며, 발사 후 늦게 도착한 마크·shot 은 무시되고 인스턴스 scope 는 정리된다.

## 2. JankStats (프레임 품질)

[jank/](../../common/presentation/src/main/java/com/jongchan/androidarchi/common/presentation/jank/) — AndroidX JankStats 기반.

- **페이지 단위는 자동**: AppNavHost가 라우트 렌더마다 `JankPageEffect(path)` 적용 — 새 화면은 등록만 하면 계측이 따라온다.
- 스크롤 리스트에는 `JankScrollWatcher(scrollableState)`를 화면에서 직접 추가 (스크롤 종료 시 구간 통계 flush). 파라미터는 `ScrollableState`라 LazyList(검색 `ContentsList`)·LazyGrid(즐겨찾기 `ContentsGrid`) 양쪽에 동일하게 쓴다.
- [JankReporter.kt](../../common/presentation/src/main/java/com/jongchan/androidarchi/common/presentation/jank/JankReporter.kt) 발사 조건: PAGE_EXIT / SCROLL_END / FROZEN_FRAME(700ms+ 즉시) / THRESHOLD_EXCEEDED(120프레임 이상 표본에서 jank 비율 5%+).
- 리포트 채널: DebugJankReport(Logcat `tag:"JankStats"`) ↔ RemoteJankReport — [JankModule](../../common/presentation/src/main/java/com/jongchan/androidarchi/common/presentation/jank/JankModule.kt)에서 `ApplicationInfo.FLAG_DEBUGGABLE`(BuildConfig.DEBUG 아님)로 분기해 바인딩 교체.

## 3. Baseline Profile / Macrobenchmark

`:baselineprofile` 모듈 — 콜드 스타트 핫 패스를 AOT 컴파일해 첫 프레임 단축.

```bash
./gradlew :app:generateBaselineProfile                     # 프로파일 재수집 (API 28+ 단말/에뮬)
./gradlew :baselineprofile:connectedBenchmarkAndroidTest   # 콜드 스타트 측정
```

- 시나리오: [BaselineProfileGenerator.kt](../../baselineprofile/src/main/java/com/jongchan/androidarchi/baselineprofile/BaselineProfileGenerator.kt) — 콜드 스타트 → 검색("kakao") → 리스트 fling 3회 → 상세(전체화면) 진입. UI 요소는 **testTag** (`search_text_field`, `search_item`)로 찾는다.
- 생성/머지된 프로파일 산출물은 `app/src/release/generated/baselineProfiles/baseline-prof.txt` 에 위치한다. `androidx.baselineprofile` 플러그인이 `:app`·`:baselineprofile` 양쪽에 적용되어 빌드 시 이 파일을 머지/패키징한다.
- **새 핵심 플로우를 추가하면 이 시나리오에 반영하고 프로파일을 재생성**한다. 화면의 testTag를 지우면 시나리오가 깨진다.
- app의 `benchmark` buildType은 release와 동일 최적화 + profileable — 수집 전용.
