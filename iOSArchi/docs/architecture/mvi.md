# MVI Architecture (`iOSArchi`)

## 1. 핵심 구성요소

`CommonPresentation`의 `MviViewModel<I: MviIntent, S: UiState, E: ReducerEvent>`를 상속하여 단방향 데이터 흐름을 강제한다.

- **`MviIntent`**: View에서 발생하는 사용자 액션의 단일 진입점 (`viewModel.onIntent(intent)`).
- **`UiState`**: 불변(`struct`, `Equatable`) 화면 상태. `@Published public private(set) var uiState: S`로 노출된다.
- **`ReducerEvent`**: 비동기 작업 결과를 상태로 투영하기 위한 내부 이벤트.
- **`dispatch(_ event: E)` → `reduce(state: S, event: E) -> S`**: 상태 변이가 일어나는 유일한 경로.

## 2. GlobalStore 연동

- 화면 간 느슨한 상태 공유가 필요할 때 `GlobalStore`를 사용한다.
- 예: `SearchViewModel`에서 검색 실행 시 `globalStore.dispatch(SampleAction(msg: ...))`를 호출하고, `FavoriteViewModel`은 `observedStateTypes: [SampleState.self]`로 구독하여 `observeGlobalState(_:)`에서 토스트를 표시한다.
