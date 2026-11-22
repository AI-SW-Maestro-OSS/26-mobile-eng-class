# Navigation & Deep Links (`iOSArchi`)

## 1. 단일 라우트 레지스트리 (`AppRouteRegistry`)

- 모든 화면은 도메인 모듈에 `Page` 프로토콜(`toRoute() -> NavRoute`)을 구현한 `struct`로 선언한다 (`IntroPage`, `SearchPage`, `FavoritePage`, `EtcPage`, `FullScreenMediaPage`).
- `MainPresentation`의 `AppRouteRegistry`가 모든 `AppRoute`를 등록하고 `GenericNavKey` 백스택을 관리한다.
- ViewModel에서는 오직 `navigationHelper.navigateTo(Page)` 또는 `navigationHelper.navigateToBack()`만 호출한다.

## 2. Cold / Warm 딥링크 및 `syntheticStack`

- **Cold Start** (또는 `/intro` 초기 상태): 대상 `AppRoute`의 `syntheticStack`을 먼저 생성한 뒤 대상 화면을 push한다.
  - 예: `app://example.architecture/fullScreenMedia?origin=FAVORITE&initialIndex=0`으로 진입 시 `[SearchPage, FavoritePage, FullScreenMediaPage]` 스택이 합성된다.
- **Warm Start**: 현재 백스택 위에 대상 화면을 push한다.

## 3. UIKit `UIViewController` 호스팅 (`ViewControllerHostContainer`)

- `FullScreenMediaViewController`는 UIKit `UIViewController` + 가로 페이징 `UICollectionView`(`FullScreenMediaCollectionDataSource`)로 구현되어 있으며, `ViewControllerHostContainer`(`UIViewControllerRepresentable`)를 통해 SwiftUI 네비게이션 스택 내부에 호스팅된다.
