# Module Structure (`iOSArchi`)

`iOSArchi`는 `AndroidArchi`의 30개 멀티모듈 구조를 Swift Package Manager(`Package.swift`) + 네이티브 Xcode 프로젝트(`iOSArchi.xcodeproj`)로 1:1 이식한 클린 아키텍처 템플릿이다.

## 1. 레이어 및 모듈 구성

모든 기능(feature)은 4개의 타겟 세트로 구성된다:
- `{feature}/entity/Sources` (`{Feature}Entity`)
- `{feature}/domain/Sources` (`{Feature}Domain`)
- `{feature}/data/Sources` (`{Feature}Data`)
- `{feature}/presentation/Sources` (`{Feature}Presentation`)

공통 및 인프라 타겟:
- `TTI` (`tti/Sources`)
- `CommonEntity`, `CommonDomain`, `CommonData`, `CommonPresentation`
- `MainEntity`, `MainDomain`, `MainData`, `MainPresentation`
- `app` (`iOSArchi.xcodeproj` 앱 진입점 및 `AppDIAssembler`)

## 2. 의존성 방향 불변 규칙

```text
presentation ──► domain ──► entity
data         ──► domain ──► entity
```

- `presentation` ↔ `data` 간 직접 의존은 엄격히 금지된다.
- `entity`, `domain`, `TTI`는 순수 Swift(`Foundation`)만 의존하며 `UIKit` / `SwiftUI`를 임포트하지 않는다.
- Feature 간 수평 의존은 금지되며, 공통 모델이나 유스케이스는 `CommonEntity` / `CommonDomain`에 위치한다.
