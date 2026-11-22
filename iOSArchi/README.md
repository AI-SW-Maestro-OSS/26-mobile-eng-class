# iOSArchi

멀티모듈 클린아키텍처 iOS 템플릿 프로젝트 (`AndroidArchi`의 1:1 현대화 iOS 포팅 버전).
Swift Package Manager(`Package.swift`) + 네이티브 Xcode 프로젝트(`iOSArchi.xcodeproj`, Tuist 미사용)로 구성되어 있으며, 4개 레퍼런스 feature(`intro` / `search` / `favorite` / `fullScreenMedia`)와 진단 탭(`etc`)이 각각 서로 다른 아키텍처 패턴의 골든 예제 역할을 수행한다.

---

## AndroidArchi ↔ iOSArchi 아키텍처 매핑

| 영역 | AndroidArchi | iOSArchi |
|---|---|---|
| **패키지 / 모듈 관리** | Gradle Multi-Module (`settings.gradle.kts`, 30개 모듈) | Swift Package Manager (`Package.swift`, 29개 타겟) + Native `.xcodeproj` (`app`) |
| **클린 아키텍처 레이어** | `<feature>/{entity,domain,data,presentation}` | `<feature>/{entity,domain,data,presentation}` (`{Feature}Entity`, `{Feature}Domain`, `{Feature}Data`, `{Feature}Presentation`) |
| **DI (의존성 주입)** | Hilt (`@Singleton`, `@ViewModelScoped`, `@Named`, `@AssistedInject`) | `DIContainer` + `DIModule` (`.singleton`, `.scoped`, `.transient`, `named:`, `FullScreenMediaViewModel.Factory`) |
| **MVI 아키텍처** | `MviViewModel<I, S, E>` (`StateFlow`, `onIntent`, `dispatch` → `reduce`) | `@MainActor MviViewModel<I, S, E>: ObservableObject` (`@Published uiState`, `onIntent`, `dispatch` → `reduce`) |
| **선언형 UI + 레거시 호스팅** | Jetpack Compose + `FragmentHostContainer` (`FullScreenMediaFragment` + `ViewPager2`) | SwiftUI + `ViewControllerHostContainer` (`UIViewControllerRepresentable` + UIKit `FullScreenMediaViewController` + `UICollectionView` Pager) |
| **네비게이션 & 딥링크** | Navigation3 (`GenericNavKey`, `AppRouteRegistry`, `RoutePattern`, `RouteMatcher`, `syntheticStack`) | SwiftUI State-Driven Stack (`GenericNavKey`, `AppRouteRegistry`, `RoutePattern`, `RouteMatcher`, `syntheticStack`) |
| **네트워크 & 직렬화** | Retrofit + OkHttp + `kotlinx.serialization` (`JsonElement`, nullable DTO → `toVO()`) | `HTTPClient` (`URLSession` async/await) + `Codable` (`ArchiJson`, `JsonElement`, nullable DTO → `toVO()`) |
| **비동기 스트림 & 전역 스토어** | Coroutines + `Flow` / `StateFlow` + `GlobalStore` | Swift Concurrency (`async/await`, `AsyncStream`, `AsyncTaskScope`) + `GlobalStore` |
| **성능 측정 (TTI / Jank)** | `:tti` 모듈 + `JankStats` | `TTI` 타겟 + `CADisplayLink` 기반 `JankReporter` (`JankPageEffect`) |

---

## 골든 예제 Feature 매트릭스

| Feature | 모듈 세트 | 핵심 아키텍처 패턴 |
|---|---|---|
| **intro** | `Intro{Entity,Domain,Data,Presentation}` | 가장 단순한 1-API MVI 화면, 도메인 에러(`IntroErrorType`) 알럿 처리, 완료 후 `/search` 진입 |
| **search** | `Search{Entity,Domain,Data,Presentation}` | 검색어 입력 + 페이지네이션(`SearchUseCase`), SDUI 배너(`SearchPageUseCase`), 즐겨찾기 스트림 동기화, `GlobalStore` 액션 발행(`SampleAction`), Metric 로깅 |
| **favorite** | `Favorite{Entity,Domain,Data,Presentation}` | 로컬 영속 저장소(`FavoriteKVStorage` + `UserDefaults` + `AsyncStream`), `GlobalStore` 상태 구독(`SampleState` → Toast 노출) |
| **fullScreenMedia** | `FullScreenMedia{Entity,Domain,Data,Presentation}` | UIKit `FullScreenMediaViewController`(`UIViewController` + 가로 페이징 `UICollectionView`)를 SwiftUI `ViewControllerHostContainer`로 호스팅, `Factory` 기반 런타임 인자 주입, 6단계 TTI 타임라인 측정 |
| **etc** | `Etc{Entity,Domain,Data,Presentation}` | `RemoteConfigRepository` (`RemoteConfigKey<T>`) 타입 안전 조회 및 토스트/알럿 테스트 |

---

## Kakao 검색 API Key 발급 및 적용 방법

`search` / `fullScreenMedia` 레퍼런스 기능은 [Kakao Daum 검색 OpenAPI](https://developers.kakao.com/docs/latest/ko/daum-search/dev-guide) (`v2/search/image`, `v2/search/vclip`)를 사용합니다. 보안을 위해 저장소에는 API Key가 포함되어 있지 않으므로, 로컬에서 실제 검색 기능을 테스트하려면 아래 절차에 따라 키를 발급받아 주입해야 합니다.

### 1. REST API Key 발급

1. [Kakao Developers 콘솔](https://developers.kakao.com/console/app)에 접속하여 로그인합니다.
2. **애플리케이션 추가하기**를 눌러 새 앱을 생성합니다.
3. 생성한 앱의 **앱 설정 → 앱 키** 메뉴에서 **REST API 키**를 복사합니다.

### 2. iOS 프로젝트에 적용하기

`AppConfig` (`common/data/Sources/config/AppConfig.swift`)는 `ProcessInfo` **환경변수 → `Info.plist`** 순서로 `API_KEY`를 조회하며, `NetworkModule`이 모든 요청에 `Authorization: KakaoAK {API_KEY}` 헤더를 자동으로 첨부합니다.

#### 방법 A. Xcode Scheme 환경변수 주입 (권장)

1. Xcode에서 `iOSArchi.xcodeproj`를 엽니다.
2. 상단 메뉴 **Product → Scheme → Edit Scheme...** (`⌘<`)을 선택합니다.
3. 좌측 **Run** → 상단 **Arguments** 탭 → **Environment Variables** 섹션에서 `API_KEY` 항목을 체크(활성화)하고 `Value`에 복사한 **REST API 키**를 입력합니다.
4. 앱을 실행(`⌘R`)하고 `Search` 탭에서 검색어를 입력해 결과가 정상 노출되는지 확인합니다.

> **보안 주의**: `iOSArchi.xcodeproj/xcshareddata/xcschemes/iOSArchi.xcscheme`은 Git에 공유되는 스킴 파일입니다. 키를 입력한 뒤 `iOSArchi.xcscheme` 변경사항이 커밋·푸시되지 않도록 주의하세요. (또는 `Edit Scheme` 하단의 `Shared` 체크를 해제하거나 개인 스킴을 복제해 사용하는 것을 권장합니다.)

#### 방법 B. `app/Info.plist`에 직접 추가

`app/Info.plist`에 아래 키-값을 추가해도 동일하게 동작합니다. (마찬가지로 실제 키가 담긴 `Info.plist`는 커밋하지 마세요.)

```xml
<key>API_KEY</key>
<string>발급받은_REST_API_KEY</string>
```

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
- [docs/architecture/error-handling.md](docs/architecture/error-handling.md) — `HTTPResponseError` 전파 및 `BaseUseCase` 에러 처리 체인
- [docs/architecture/data-layer.md](docs/architecture/data-layer.md) — DTO/VO 변환, `BaseRemoteDataSource`, `DIModule`, 로컬 저장소
- [docs/architecture/design-system.md](docs/architecture/design-system.md) — 디자인 토큰(`DesignTokens.swift`), 시맨틱 컬러, 타이포그래피
- [docs/architecture/performance.md](docs/architecture/performance.md) — TTI 6단계 측정 및 `CADisplayLink` 기반 Jank 리포팅
- [docs/METRIC_LOGGING_GUIDE.md](docs/METRIC_LOGGING_GUIDE.md) — Metric / TTI 로깅 → Google SpreadSheet 연동 가이드
