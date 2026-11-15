# Performance (`iOSArchi`)

## 1. TTI (Time-To-Interactive) 측정

- `TTI` 모듈의 `TTIHelperImpl`은 `@ViewModelScoped`로 주입되어 하나의 화면(`ViewModel` + 해당 `UseCase`들)에서 동일한 인스턴스를 공유한다.
- `FullScreenMediaTTIPage`는 6개 타임라인(`TTI_TIME`, `VIEW_CREATION_TIME`, `API_REQUEST_READY_TIME`, `API_RESPONSE_TIME`, `VIEW_BINDING_TIME`, `IMAGE_LOADED_TIME`)을 측정하며 20초 타임아웃 워치독을 내장한다.

## 2. JankStats (`JankReporter`)

- iOS `CADisplayLink`를 이용해 프레임 간 소요 시간(> 16.67ms)을 추적하고, 화면 이탈 시 총 프레임 수, Jank 비율, P90/P95/Max 프레임 시간을 `JankSnapshot`으로 집계한다.
- 각 페이지 루트 View에 `.jankPageEffect(Page.PATH)` 수정자를 부착하여 자동 추적한다.
