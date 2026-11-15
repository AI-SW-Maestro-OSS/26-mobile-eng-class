# iOSArchi

멀티모듈 클린아키텍처 iOS 템플릿 프로젝트 (`AndroidArchi`의 iOS 포팅 버전). 4개 레퍼런스 feature(`intro` / `search` / `favorite` / `fullScreenMedia`)는 각각 서로 다른 패턴의 골든 예제다 — 새 코드는 항상 가장 비슷한 골든 예제를 복제한다.

## 빌드 / 테스트

```bash
swift test    # 전체 모듈 컴파일 및 단위 테스트
xcodebuild -project iOSArchi.xcodeproj -scheme iOSArchi -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

코드를 수정한 턴은 반드시 빌드 검증 후 종료한다. API 키/BASE_URL은 환경 변수 또는 Info.plist(`API_KEY`, `API_BASE_URL`) → `BuildConfig` 주입.

## 불변 규칙 (위반 금지)

1. **의존 방향**: `presentation → domain → entity`, `data → domain → entity`. presentation↔data 직접 의존 금지. entity/domain은 순수 Swift/Foundation(UIKit/SwiftUI 의존 금지).
2. **feature = 4모듈**: `<feature>/{entity,domain,data,presentation}` (`Package.swift` 내 `{Feature}Entity`, `{Feature}Domain`, `{Feature}Data`, `{Feature}Presentation`).
3. **MVI**: View→`onIntent(Intent)` 단일 진입, 상태 변이는 `dispatch(ReducerEvent)`→`reduce()` 한 곳에서만.
4. **디자인 토큰**: 색은 `DesignSystemThemeImpl.designSystemColor.*`(`ArchiThemeImpl.archiColor.*`), 타이포는 `DesignSystemThemeImpl.typeScale.*`(`ArchiText` 경유)만. 화면 코드 내 raw hex / raw font size 금지. 토큰 기본값 수정은 `common/presentation/Sources/ui/token/DesignTokens.swift`의 `FIGMA-TOKEN-INJECTION-POINT` 구간만.
5. **DTO/VO**: DTO는 `Codable`+전 필드 Optional, VO는 비-Optional+기본값. 변환은 data 레이어 `toVO()`에서만.
6. **에러**: data는 `HttpResponseException` throw만, 처리(다이얼로그/네비게이션/토스트)는 domain UseCase에서 `isCommonErrorHandling()`/`handlingErrorOnUseCase(ErrorType.self)`로.
7. **네비게이션**: 화면 이동은 `navigationHelper.navigateTo(Page)`만. 새 화면은 `AppRouteRegistry.swift`에 등록. 기존 feature 모듈은 수정하지 않고 추가만.

## 네이밍

`{Feature}Page(=domain의 Page struct & presentation의 PageView) / {Feature}UIState / {Feature}Intent / {Feature}ReducerEvent / {Feature}ViewModel / {Feature}Repository(Impl) / {Feature}DataSource / {Feature}ApiService / {Feature}DTO / {Feature}VO / {Feature}DataModule / {Feature}ErrorType(domain에 위치)`

## 상세 문서

- [docs/architecture/module-structure.md](docs/architecture/module-structure.md) — 모듈 규칙, `Package.swift` 타겟 구조
- [docs/architecture/mvi.md](docs/architecture/mvi.md) — `MviViewModel` 계약, 페이징 상태 패턴
- [docs/architecture/navigation.md](docs/architecture/navigation.md) — `GenericNavKey`, `syntheticStack`, 딥링크, UIKit `UIViewController` 호스팅
- [docs/architecture/error-handling.md](docs/architecture/error-handling.md) — 에러 전파/처리 체인
- [docs/architecture/data-layer.md](docs/architecture/data-layer.md) — DTO/VO, DataSource, `DIModule`, 로컬 저장소
- [docs/architecture/design-system.md](docs/architecture/design-system.md) — 토큰 구조, Figma 매핑 명세
- [docs/architecture/performance.md](docs/architecture/performance.md) — TTI, JankReporter
- [docs/METRIC_LOGGING_GUIDE.md](docs/METRIC_LOGGING_GUIDE.md) — Metric 로깅 → Google SpreadSheet(Apps Script) 세팅/사용 가이드
