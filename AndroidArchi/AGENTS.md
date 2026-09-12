# AndroidArchi

멀티모듈 클린아키텍처 Android 템플릿 프로젝트. 4개 레퍼런스 feature(intro/search/favorite/fullScreenMedia)는 각각 서로 다른 패턴의 골든 예제다 — 새 코드는 항상 가장 비슷한 골든 예제를 복제한다 (표는 README 참고).

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

## 상세 문서

- [docs/architecture/module-structure.md](docs/architecture/module-structure.md) — 모듈 규칙, build.gradle.kts 3종 템플릿
- [docs/architecture/mvi.md](docs/architecture/mvi.md) — MviViewModel 계약, 페이징 상태 패턴
- [docs/architecture/navigation.md](docs/architecture/navigation.md) — Navigation3, synthetic backstack, 딥링크, Fragment 호스팅
- [docs/architecture/error-handling.md](docs/architecture/error-handling.md) — 에러 전파/처리 체인
- [docs/architecture/data-layer.md](docs/architecture/data-layer.md) — DTO/VO, DataSource, Hilt DataModule, 로컬 저장소
- [docs/architecture/design-system.md](docs/architecture/design-system.md) — 토큰 구조, Figma 매핑 명세
- [docs/architecture/performance.md](docs/architecture/performance.md) — TTI, JankStats, Baseline Profile
- [docs/DESIGN_TO_CODE_GUIDE.md](docs/DESIGN_TO_CODE_GUIDE.md) — 디자인 스펙 → 코드 사용자 가이드
- [docs/METRIC_LOGGING_GUIDE.md](docs/METRIC_LOGGING_GUIDE.md) — Metric 로깅 → Google SpreadSheet(Apps Script) 세팅/사용 가이드


## Subagent 운영

- Kotlin/Gradle 기능 코드를 수정한 뒤 최종 보고 또는 커밋 전: `architecture-guardian`으로 변경 파일을 리뷰한다.
- `architecture-guardian`이 `HIGH` 또는 `MEDIUM`으로 보고한 항목은 수정한 뒤 `gradle-build-check`를 다시 실행한다. 심각도 기준은 `.codex/agents/architecture-guardian.toml`에 정의되어 있다.
- Gradle/Kotlin 빌드 또는 테스트가 실패하고 첫 번째 에러만으로 즉시 수정 방향이 명확하지 않거나 로그가 길 때: `build-fixer`에 원래 실패한 명령과 핵심 에러를 전달해 수정하게 한다.
- subagent를 사용할 수 없으면 같은 검토를 메인 에이전트가 직접 수행하고, 최종 보고에 미사용 사유를 적는다.

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
