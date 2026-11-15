# Design System (`iOSArchi`)

## 1. 토큰 단일 진실 공급원 (`DesignTokens.swift`)

`common/presentation/Sources/ui/token/DesignTokens.swift` 파일이 팔레트 컬러, 시맨틱 컬러, 타이포그래피 스케일의 유일한 정의 지점이다:
- `// FIGMA-TOKEN-INJECTION-POINT: palette` (`ArchiPaletteColors`)
- `// FIGMA-TOKEN-INJECTION-POINT: semantic-colors` (`DefaultDesignSystemColor`)
- `// FIGMA-TOKEN-INJECTION-POINT: type-scale` (`DefaultDesignSystemStaticTypeScale`)

## 2. 사용 규칙

- 모든 화면과 컴포넌트는 `DesignSystemThemeImpl.designSystemColor.*` (또는 `ArchiThemeImpl.archiColor.*`)와 `DesignSystemThemeImpl.typeScale.*` (`ArchiText` 컴포넌트 경유)만 사용한다.
- 화면/컴포넌트 파일에서 raw hex 색상이나 raw 폰트 크기를 직접 사용하는 것은 금지된다.
