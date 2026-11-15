# iOSArchi

멀티모듈 클린아키텍처 iOS 템플릿 프로젝트 (`AndroidArchi`의 1:1 현대화 iOS 포팅 버전).
Swift Package Manager(`Package.swift`) + 네이티브 Xcode 프로젝트(`iOSArchi.xcodeproj`, Tuist 미사용)로 구성되어 있으며, 4개 레퍼런스 feature(`intro` / `search` / `favorite` / `fullScreenMedia`)와 진단 탭(`etc`)이 각각 서로 다른 아키텍처 패턴의 골든 예제 역할을 수행한다.

---

## AndroidArchi ↔ iOSArchi 아키텍처 매핑

| 영역 | AndroidArchi | iOSArchi |
|---|---|---|
| **패키지 / 모듈 관리** | Gradle Multi-Module (`settings.gradle.kts`, 30개 모듈) | Swift Package Manager (`Package.swift`, 29개 타겟) + Native `.xcodeproj` (`app`) |
| **클린 아키텍처 레이어** | `<feature>/{entity,domain,data,presentation}` | `<feature>/{entity,domain,data,presentation}` (`{Feature}Entity`, `{Feature}Domain`, `{Feature}Data`, `{Feature}Presentation`) |
| **DI (의존성 주입)** | Hilt (`@Singleton`, `@ViewModelScoped`, `@Named`, `@AssistedInject`) | `DIContainer` + `DIModule` (`.singleton`, `.viewModelScoped`, `.transient`, `named:`, `FullScreenMediaViewModel.Factory`) |
| **MVI 아키텍처** | `MviViewModel<I, S, E>` (`StateFlow`, `onIntent`, `dispatch` → `reduce`) | `@MainActor MviViewModel<I, S, E>: ObservableObject` (`@Published uiState`, `onIntent`, `dispatch` → `reduce`) |
| **선언형 UI + 레거시 호스팅** | Jetpack Compose + `FragmentHostContainer` (`FullScreenMediaFragment` + `ViewPager2`) | SwiftUI + `FragmentHostContainer` (`UIViewControllerRepresentable` + UIKit `FullScreenMediaFragment` + `UICollectionView` Pager) |
| **네비게이션 & 딥링크** | Navigation3 (`GenericNavKey`, `AppRouteRegistry`, `RoutePattern`, `RouteMatcher`, `syntheticStack`) | SwiftUI State-Driven Stack (`GenericNavKey`, `AppRouteRegistry`, `RoutePattern`, `RouteMatcher`, `syntheticStack`) |
| **네트워크 & 직렬화** | Retrofit + OkHttp + `kotlinx.serialization` (`JsonElement`, nullable DTO → `toVO()`) | `RetrofitClient` (`URLSession` async/await) + `Codable` (`ArchiJson`, `JsonElement`, nullable DTO → `toVO()`) |
| **비동기 스트림 & 전역 스토어** | Coroutines + `Flow` / `StateFlow` + `GlobalStore` | Swift Concurrency (`async/await`, `AsyncStream`, `ArchiCoroutineScope`) + `GlobalStore` |
| **성능 측정 (TTI / Jank)** | `:tti` 모듈 + `JankStats` | `TTI` 타겟 + `CADisplayLink` 기반 `JankReporter` (`JankPageEffect`) |

---

## 골든 예제 Feature 매트릭스

| Feature | 모듈 세트 | 핵심 아키텍처 패턴 |
|---|---|---|
| **intro** | `Intro{Entity,Domain,Data,Presentation}` | 가장 단순한 1-API MVI 화면, 도메인 에러(`IntroErrorType`) 다이얼로그 처리, 완료 후 `/search` 진입 |
| **search** | `Search{Entity,Domain,Data,Presentation}` | 검색어 입력 + 페이지네이션(`SearchUseCase`), SDUI 배너(`SearchPageUseCase`), 즐겨찾기 스트림 동기화, `GlobalStore` 액션 발행(`SampleAction`), Metric 로깅 |
| **favorite** | `Favorite{Entity,Domain,Data,Presentation}` | 로컬 영속 저장소(`FavoriteKVStorage` + `UserDefaults` + `AsyncStream`), `GlobalStore` 상태 구독(`SampleState` → Toast 노출) |
| **fullScreenMedia** | `FullScreenMedia{Entity,Domain,Data,Presentation}` | UIKit `FullScreenMediaFragment`(`UIViewController` + 가로 페이징 `UICollectionView`)를 SwiftUI `FragmentHostContainer`로 호스팅, `Factory` 기반 런타임 인자 주입, 6단계 TTI 타임라인 측정 |
| **etc** | `Etc{Entity,Domain,Data,Presentation}` | `RemoteConfigRepository` (`RemoteConfigKey<T>`) 타입 안전 조회 및 토스트/다이얼로그 테스트 |

---

## 빌드 및 테스트 명령어

```bash
# 1. 전체 SPM 모듈 빌드 및 단위 테스트 실행 (29개 모듈 + 4개 테스트 타겟)
swift test

# 2. iOS Simulator 용 앱 타겟(iOSArchi.app) 빌드 검증
xcodebuild -project iOSArchi.xcodeproj \
  -scheme iOSArchi \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO \
  build
```

---

## 상세 아키텍처 문서

- [docs/architecture/module-structure.md](docs/architecture/module-structure.md) — 모듈 구조 및 의존성 방향 규칙
- [docs/architecture/mvi.md](docs/architecture/mvi.md) — `MviViewModel` 계약 및 `GlobalStore` 연동
- [docs/architecture/navigation.md](docs/architecture/navigation.md) — `AppRouteRegistry`, `syntheticStack`, 딥링크 Cold/Warm 정책, UIKit 호스팅
- [docs/architecture/error-handling.md](docs/architecture/error-handling.md) — `HttpResponseException` 전파 및 `BaseUseCase` 에러 처리 체인
- [docs/architecture/data-layer.md](docs/architecture/data-layer.md) — DTO/VO 변환, `BaseRemoteDataSource`, `DIModule`, 로컬 저장소
- [docs/architecture/design-system.md](docs/architecture/design-system.md) — 디자인 토큰(`DesignTokens.swift`), 시맨틱 컬러, 타이포그래피
- [docs/architecture/performance.md](docs/architecture/performance.md) — TTI 6단계 측정 및 `CADisplayLink` 기반 Jank 리포팅
- [docs/METRIC_LOGGING_GUIDE.md](docs/METRIC_LOGGING_GUIDE.md) — Metric / TTI 로깅 → Google SpreadSheet 연동 가이드
