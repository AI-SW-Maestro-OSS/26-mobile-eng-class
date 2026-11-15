# Data Layer (`iOSArchi`)

## 1. DTO / VO 분리 원칙

- **DTO (`{Feature}DTO` / `*ResponseDTO`)**:
  - `data` 모듈 내부에만 위치하며 `Codable`을 채택한다.
  - 서버 응답 누락에 안전하도록 **모든 프로퍼티를 Optional(`?`)**로 선언한다.
  - `toVO()` 매퍼를 통해 기본값이 채워진 비-Optional `VO`로 변환한다.
- **VO (`{Feature}VO`)**:
  - `entity` 또는 `domain` 모듈에 위치하며 모든 프로퍼티가 비-Optional이다.

## 2. 로컬 저장소 (`FavoriteKVStorage`)

- `UserDefaults` + `ArchiJson` 직렬화 및 `AsyncStream<[FavoriteItemDto]>`를 통해 앱 재시작 후에도 즐겨찾기 목록을 유지하고 실시간 스트림으로 방출한다.
