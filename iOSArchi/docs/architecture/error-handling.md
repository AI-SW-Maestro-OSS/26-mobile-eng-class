# Error Handling (`iOSArchi`)

## 1. 에러 전파 체인

1. **Data Layer (`BaseRemoteDataSource.checkResponse`)**:
   - HTTP 응답이 2xx가 아니거나 본문이 비어있으면 `HttpResponseException`을 throw한다.
   - Data 레이어는 UI(다이얼로그/토스트)를 절대 직접 호출하지 않는다.
2. **Domain Layer (`BaseUseCase`)**:
   - 각 feature의 `domain` 모듈에 `HttpErrorType`을 구현한 `{Feature}ErrorType` enum을 정의한다.
   - UseCase의 `catch let e as HttpResponseException` 블록에서:
     1. `e.handlingErrorOnUseCase({Feature}ErrorType.self)`로 도메인 특화 에러를 우선 처리한다.
     2. 해당하지 않으면 `e.isCommonErrorHandling()`을 확인하고 `executeCommonErrorHanding(e)`(401 세션 만료 → 초기 화면 이동, 404 미지원 기능 → 뒤로 이동, 5xx 일시적 오류 다이얼로그)를 실행한다.
