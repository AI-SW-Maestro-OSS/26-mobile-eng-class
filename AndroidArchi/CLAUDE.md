# AndroidArchi

멀티모듈 클린아키텍처 Android **템플릿** 프로젝트. 4개 레퍼런스 feature(intro/search/favorite/fullScreenMedia)는 각각 서로 다른 패턴의 골든 예제다 — 새 코드는 항상 가장 비슷한 골든 예제를 복제한다 (표는 README 참고).

## 빌드 / 테스트

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"  # 시스템 JDK 없을 때
./gradlew :app:assembleDebug    # 빌드 검증 (코드 수정 후 필수 — gradle-build-check 스킬)
./gradlew test                  # 단위 테스트 (run-android-tests 스킬)
```

코드를 수정한 턴은 반드시 빌드 검증 후 종료한다. API 키/BASE_URL은 `local.properties`(`API_KEY`, `API_BASE_URL`) → BuildConfig 주입.

## 불변 규칙 (위반 금지)

1. **의존 방향**: `presentation → domain → entity`, `data → domain → entity`. presentation↔data 직접 의존 금지. entity/domain은 순수 Kotlin/JVM(Android 의존 금지).
2. **feature = 4모듈**: `<feature>/{entity,domain,data,presentation}`. 생성은 `make-new-feature-module` 스킬로만.
3. **MVI**: View→`onIntent(Intent)` 단일 진입, 상태 변이는 `dispatch(ReducerEvent)`→`reduce()` 한 곳에서만. UIState 컬렉션은 ImmutableList/ImmutableSet.
4. **디자인 토큰**: 색은 `ArchiThemeImpl.archiColor.*`, 타이포는 `ArchiThemeImpl.typeScale.*`(ArchiText 경유)만. raw hex / raw sp 금지. 토큰 기본값 수정은 `common/presentation/.../ui/token/DesignTokens.kt`의 FIGMA-TOKEN-INJECTION-POINT 구간만.
5. **DTO/VO**: DTO는 `@Serializable`+전 필드 nullable, VO는 비-nullable+기본값. 변환은 data 레이어 `toVO()`에서만.
6. **에러**: data는 `HttpResponseException` throw만, 처리(다이얼로그/네비게이션)는 domain UseCase에서 `isCommonErrorHandling()`/`handlingErrorOnUseCase<ErrorType>()`로.
7. **네비게이션**: 화면 이동은 `navigationHelper.navigateTo(Page)`만. 새 화면은 `AppRouteRegistry.kt`에 등록. 기존 feature 모듈은 수정하지 않고 추가만.

## 네이밍

`{Feature}Page(=Composable & domain의 Page object) / {Feature}UIState / {Feature}Intent / {Feature}ReducerEvent / {Feature}ViewModel / {Feature}Repository(Impl) / {Feature}DataSource / {Feature}ApiService / {Feature}DTO / {Feature}VO / {Feature}DataModule / {Feature}ErrorType(domain에 위치)`

## 상세 문서 (패턴별 왜/어떻게/골든 예제)

- [docs/architecture/module-structure.md](docs/architecture/module-structure.md) — 모듈 규칙, build.gradle.kts 3종 템플릿
- [docs/architecture/mvi.md](docs/architecture/mvi.md) — MviViewModel 계약, 페이징 상태 패턴
- [docs/architecture/navigation.md](docs/architecture/navigation.md) — Navigation3, synthetic backstack, 딥링크, Fragment 호스팅
- [docs/architecture/error-handling.md](docs/architecture/error-handling.md) — 에러 전파/처리 체인
- [docs/architecture/data-layer.md](docs/architecture/data-layer.md) — DTO/VO, DataSource, Hilt DataModule, 로컬 저장소
- [docs/architecture/design-system.md](docs/architecture/design-system.md) — 토큰 구조, Figma 매핑 명세
- [docs/architecture/performance.md](docs/architecture/performance.md) — TTI, JankStats, Baseline Profile
- [docs/DESIGN_TO_CODE_GUIDE.md](docs/DESIGN_TO_CODE_GUIDE.md) — 디자인 스펙 → 코드 사용자 가이드
- [docs/METRIC_LOGGING_GUIDE.md](docs/METRIC_LOGGING_GUIDE.md) — Metric 로깅 → Google SpreadSheet(Apps Script) 세팅/사용 가이드

## 스킬 인덱스

| 작업 | 스킬 |
|---|---|
| 디자인 스펙(Figma/이미지/PDF) → feature 전체 | `design-to-feature` |
| Figma 토큰 → 테마 갱신 | `design-token-sync` |
| 새 feature 4모듈 스캐폴딩 | `make-new-feature-module` |
| 네비게이션/라우트/딥링크 규칙 (화면·경로·딥링크 추가/수정 시) | `navigation-conventions` |
| 예시 JSON → 데이터 레이어 | `api-dto-code-gen` |
| Composable 1개 추가 | `compose-component` |
| 빌드 검증 (수정 후 필수) | `gradle-build-check` |
| 테스트 실행 | `run-android-tests` |
| 템플릿 → 신규 프로젝트 (디자인 스펙 Figma/이미지/PDF 동반 시 토큰+화면 생성까지 연계) | `new-project-from-archi` |
| 에뮬레이터 화면 vs 디자인 비교 | `verify-screen` |
