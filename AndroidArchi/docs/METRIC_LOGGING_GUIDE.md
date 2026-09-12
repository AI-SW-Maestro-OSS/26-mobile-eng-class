# Metric Logging → Google SpreadSheet 가이드

앱에서 발생하는 사용자 행동 지표(Metric)를 **Google SpreadSheet** 에 행 단위로 쌓는 방법입니다.
별도 백엔드·분석 SDK 없이, 시트에 붙인 **Google Apps Script Web App** 이 수신 엔드포인트 역할을 합니다.

```
[앱]                                   [Google]
LoggingHelper.shotMetricLogging(event, params)
   │  (presentation, fire-and-forget)
   ▼
MetricLoggingRepository ──▶ MetricLoggingRepositoryImpl ──HTTP POST(JSON)──▶ Apps Script doPost(e)
   (domain 인터페이스)         (data, Retrofit)                                  │ 토큰 검증
                                                                                │ event 이름의 탭 찾기
                                                                                │ 1행 헤더 순서로 row 구성
                                                                                ▼
                                                                    SpreadSheet `LoggingBucket`
                                                                    탭 = event.name, 1행 = 헤더
```

핵심 규칙 3가지만 기억하면 됩니다.

| 항목 | 규칙 |
|---|---|
| 시트 탭 이름 | `MetricLogging.name` 과 **정확히 동일** (예: `click_favorite_toggle_search_page`) |
| 1행 헤더 | `timestamp` + `MetricLoggingParam.name` 들 (예: `all_favorite_item_count`, `item_img_url`, `search_keyword`) |
| `timestamp` | 앱이 보내지 않음. Apps Script 가 **실행 시각**으로 채움 |

---

## 1. Google SpreadSheet 만들기

### 1.1 시트 생성

1. [Google Sheets](https://docs.google.com/spreadsheets) 에서 새 스프레드시트를 만들고 이름을 `LoggingBucket` 으로 바꿉니다. (이름은 자유지만, 이 문서와 코드 주석은 `LoggingBucket` 기준입니다.)
2. 스프레드시트 하나가 "로깅 버킷" 전체이고, **탭(시트) 하나가 이벤트 하나**입니다.

### 1.2 이벤트 탭 만들기

이벤트마다 탭을 하나 추가하고 아래처럼 구성합니다.

| | A | B | C | D |
|---|---|---|---|---|
| **1** | `timestamp` | `all_favorite_item_count` | `item_img_url` | `search_keyword` |
| 2 | (스크립트가 append) | | | |

- **탭 이름** = 앱의 `MetricLogging.name` 값. 오타·대소문자·공백이 하나라도 다르면 스크립트가 `unknown event` 로 거부합니다.
- **A1 부터 1행** = 헤더. `timestamp` 는 항상 넣고, 나머지는 앱의 `MetricLoggingParam.name` 값을 그대로 씁니다. 컬럼 순서는 자유입니다 — 스크립트가 헤더 이름으로 매핑합니다.
- 헤더에 없는 파라미터는 버려지고, 파라미터가 없는 헤더는 빈 칸으로 들어갑니다. 즉 시트와 앱이 조금 어긋나도 깨지지 않습니다.
- `timestamp` 컬럼은 `서식 → 숫자 → 날짜 시간` 으로 지정해 두면 보기 편합니다.

### 1.3 권한

시트는 **공유 설정을 바꿀 필요가 없습니다.** Apps Script 가 시트 소유자 권한으로 실행되므로(아래 2.4 "실행 사용자: 나") 시트는 비공개여도 됩니다. 결과를 같이 볼 사람에게만 뷰어로 공유하세요.

---

## 2. Apps Script 세팅

### 2.1 스크립트 열기

스프레드시트 메뉴 `확장 프로그램 → Apps Script`. 시트에 **바인딩된** 스크립트 프로젝트가 열립니다. (독립형 스크립트를 쓰면 `SpreadsheetApp.getActiveSpreadsheet()` 가 동작하지 않으니 반드시 시트에서 여세요.)

### 2.2 코드

`Code.gs` 내용을 아래로 교체합니다.

```javascript
function doPost(e) {
  const lock = LockService.getScriptLock();
  lock.waitLock(10000); // 동시 요청 시 append 순서 보호

  try {
    const body = JSON.parse(e.postData.contents);

    // 1) 토큰 검증 — 스크립트 속성 TOKEN 과 일치해야 한다.
    const expected = PropertiesService.getScriptProperties().getProperty('TOKEN');
    if (!expected || body.token !== expected) {
      return json_({ ok: false, error: 'unauthorized' });
    }

    // 2) 이벤트 이름 = 탭 이름
    const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(body.event);
    if (!sheet) {
      return json_({ ok: false, error: 'unknown event: ' + body.event });
    }

    // 3) 1행 헤더 순서대로 row 구성. timestamp 는 서버(스크립트) 실행 시각.
    const headers = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
    const params = body.params || {};
    const row = headers.map(h =>
      h === 'timestamp' ? new Date() : (params[h] ?? ''));

    sheet.appendRow(row);
    return json_({ ok: true });
  } catch (err) {
    return json_({ ok: false, error: String(err) });
  } finally {
    lock.releaseLock();
  }
}

function json_(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
```

앱이 보내는 요청 본문은 다음과 같습니다. (앱 코드: [MetricLogDTO.kt](../common/data/src/main/java/com/jongchan/androidarchi/common/data/analytics/metric/dto/MetricLogDTO.kt))

```json
{
  "token": "<스크립트 속성 TOKEN>",
  "event": "click_favorite_toggle_search_page",
  "params": { "all_favorite_item_count": 3, "item_img_url": "https://...", "search_keyword": "고양이" }
}
```

### 2.3 스크립트 속성 (TOKEN)

`프로젝트 설정(⚙) → 스크립트 속성 → 속성 추가` 에 `TOKEN` 을 추가합니다.

- 값은 **직접 만드는 임의의 비밀 문자열**입니다. Google 이 발급하는 키가 아닙니다.
- 터미널에서 `openssl rand -hex 32` 로 생성하거나, 스크립트 편집기에서 `Utilities.getUuid()` 를 한 번 실행한 값을 써도 됩니다.
- 같은 값을 앱의 `local.properties` → `METRIC_LOG_TOKEN` 에 넣습니다 (3.1 참고). **절대 커밋하지 마세요.**

### 2.4 시간대

`프로젝트 설정 → 시간대` 를 `(GMT+09:00) 서울` 로 맞춥니다. `timestamp` 가 이 시간대로 기록됩니다.
(`appsscript.json` 을 편집한다면 `"timeZone": "Asia/Seoul"`.)

### 2.5 배포 (웹 앱)

1. 우측 상단 `배포 → 새 배포`.
2. `유형 선택(⚙) → 웹 앱`.
3. 설정:
   - **실행 사용자: 나** — 시트 소유자 권한으로 실행됩니다.
   - **액세스 권한이 있는 사용자: 모든 사용자** — 앱(비로그인)이 호출할 수 있어야 합니다.
4. `배포` → 처음이면 시트 접근 권한 승인 창이 뜹니다. 승인합니다.
5. 발급된 **웹 앱 URL** (`https://script.google.com/macros/s/AKfycb.../exec`) 을 복사합니다. 이 값이 앱의 `METRIC_LOG_URL` 입니다.

> **코드를 수정한 뒤에는 저장만으로 반영되지 않습니다.** `배포 → 배포 관리 → ✏️ 수정 → 버전: 새 버전 → 배포` 를 해야 같은 URL 에 새 코드가 적용됩니다.

### 2.6 curl 로 검증

앱을 붙이기 전에 엔드포인트가 동작하는지 먼저 확인합니다.

```bash
curl -sSL -X POST '<웹 앱 URL>' \
  -H 'Content-Type: application/json' \
  -d '{"token":"<TOKEN>","event":"click_favorite_toggle_search_page","params":{"all_favorite_item_count":1,"item_img_url":"https://example.com/a.png","search_keyword":"test"}}'
```

| 응답 | 의미 |
|---|---|
| `{"ok":true}` + 시트에 행 추가 | 정상 |
| `{"ok":false,"error":"unauthorized"}` | URL 은 맞음. `TOKEN` 값 불일치 |
| `{"ok":false,"error":"unknown event: ..."}` | 탭 이름 ≠ `event` |
| Google Drive "페이지를 찾을 수 없음" HTML | URL 오류(배포 ID 누락/오타, 편집기 URL 복사 등). 2.5 의 웹 앱 URL 을 다시 복사 |
| Google 로그인 HTML | 액세스 권한이 "모든 사용자" 가 아님 |

`-L` 은 필수입니다 — Apps Script 웹 앱은 POST 에 302 로 응답하고, 리다이렉트된 GET 이 실제 응답을 돌려줍니다. (앱의 OkHttp 는 기본으로 리다이렉트를 따라갑니다.)

---

## 3. 앱 프로젝트에서 사용하기

### 3.1 설정 주입

프로젝트 루트 `local.properties` 에 두 줄을 추가합니다. (이 파일은 `.gitignore` 대상입니다.)

```properties
METRIC_LOG_URL=https://script.google.com/macros/s/AKfycb.../exec
METRIC_LOG_TOKEN=<2.3 에서 만든 TOKEN>
```

[common/data/build.gradle.kts](../common/data/build.gradle.kts) 의 `METRIC-LOG-INJECTION-POINT` 가 이 값을 `BuildConfig.METRIC_LOG_URL / METRIC_LOG_TOKEN` 으로 주입합니다.
값이 비어 있으면 빌드는 정상이고, 로깅 호출은 Logcat 경고만 남기고 건너뜁니다 — 로깅 설정 없이 클론한 사람도 빌드가 깨지지 않습니다.

값을 바꾼 뒤에는 `Sync Project with Gradle Files` 또는 재빌드가 필요합니다 (BuildConfig 는 빌드 시 생성).

### 3.2 동작 확인

1. `./gradlew :app:assembleDebug` 후 디버그 앱 실행.
2. 검색 화면에서 검색 → 결과 아이템의 즐겨찾기 등록.
3. **Logcat** 에서 태그 `MetricLogging` 필터:
   - `Shot Metric Logging: event=click_favorite_toggle_search_page, type=Track, params={...}` — 발사됨 (디버그 빌드만 출력)
   - `Metric logging is disabled (METRIC_LOG_URL is empty)` — 3.1 미설정
   - `Failed to send metric logging: ... cause=...` — 네트워크/서버 오류. `cause` 에 Apps Script 의 `error` 가 그대로 담깁니다.
   - `Dropped params not declared in ...availableParams` — 이벤트에 선언되지 않은 파라미터를 넘김 (4절 참고)
4. 시트의 `click_favorite_toggle_search_page` 탭에 행이 추가됐는지 확인. 요청 본문은 OkHttp 로깅 인터셉터(`OkHttp` 태그)로도 볼 수 있습니다.

### 3.3 코드 구조

| 레이어 | 파일 | 역할 |
|---|---|---|
| domain | [MetricLogging.kt](../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/analytics/metric/MetricLogging.kt) | 이벤트 정의 (`name` = 시트 탭 이름, `type`, `availableParams`) |
| domain | [MetricLoggingParam.kt](../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/analytics/metric/MetricLoggingParam.kt) | 파라미터 정의 (`name` = 시트 헤더 이름) |
| domain | [MetricLoggingRepository.kt](../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/analytics/metric/MetricLoggingRepository.kt) | 전송 추상화 (`send(event, params): Boolean`) |
| domain | [MetricLoggingConfig.kt](../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/analytics/metric/MetricLoggingConfig.kt) | 활성 여부 (URL/토큰 존재) — presentation 이 data 의 BuildConfig 를 보지 않게 하는 seam |
| domain | [LoggingHelper.kt](../common/domain/src/main/java/com/jongchan/androidarchi/common/domain/helper/LoggingHelper.kt) | ViewModel/UseCase 가 호출하는 진입점 |
| data | [analytics/metric/](../common/data/src/main/java/com/jongchan/androidarchi/common/data/analytics/metric/) | `MetricLogDTO`, `MetricLoggingApiService`, `MetricLoggingDataSource`, `MetricLoggingRepositoryImpl`, `MetricLoggingDataModule`(`@Named("metricLogging")` Retrofit — 카카오 API 인증 인터셉터와 분리) |
| presentation | [LoggingHelperImpl.kt](../common/presentation/src/main/java/com/jongchan/androidarchi/common/presentation/helper/LoggingHelperImpl.kt) | `availableParams` 필터 → `@IoScope` 에서 fire-and-forget → 예외 전부 삼킴(Logcat 경고) |
| 사용 예 | [SearchViewModel.kt](../search/presentation/src/main/java/com/jongchan/androidarchi/search/presentation/SearchViewModel.kt) `addFavorite()` | 즐겨찾기 등록 성공 시 로깅 |

로깅은 **절대 앱 동작을 막거나 죽이면 안 된다**는 원칙으로, 호출 스레드를 막지 않고 실패는 경고 로그로만 남깁니다.

---

## 4. 새 이벤트 추가하기

시트와 앱 양쪽에 각각 한 가지씩만 하면 됩니다. **Apps Script 는 수정하지 않습니다.**

### 4.1 시트

새 탭을 추가하고 이름을 이벤트 이름으로, 1행에 `timestamp` + 파라미터 이름을 적습니다.

### 4.2 앱

1. 필요한 파라미터가 없다면 `MetricLoggingParam` 에 `data object` 를 추가합니다. `name` 은 시트 헤더와 동일하게 snake_case.
   ```kotlin
   data object ItemType : MetricLoggingParam(name = "item_type")
   ```
2. `MetricLogging` 에 이벤트 `data object` 를 추가합니다. `name` 은 시트 탭 이름과 동일, `availableParams` 에 이 이벤트가 보낼 수 있는 파라미터를 선언합니다. (선언 안 된 파라미터는 `LoggingHelperImpl` 이 버립니다.)
   ```kotlin
   data object ClickOpenFullScreenSearchPage : MetricLogging(
       name = "click_open_full_screen_search_page",
       type = MetricEventType.Track,
       availableParams = setOf(MetricLoggingParam.ItemType, MetricLoggingParam.ItemImgUrl),
   )
   ```
3. ViewModel(또는 UseCase)에서 `LoggingHelper` 를 주입받아 호출합니다.
   ```kotlin
   loggingHelper.shotMetricLogging(
       event = MetricLogging.ClickOpenFullScreenSearchPage,
       params = mapOf(
           MetricLoggingParam.ItemType to item.type.name,
           MetricLoggingParam.ItemImgUrl to item.contentsImageUrl,
       ),
   )
   ```
4. `./gradlew :app:assembleDebug` 로 빌드 검증.

파라미터 값은 `Boolean / Number / String` 은 그대로, 그 외 타입은 `toString()` 으로 전송됩니다.

---

## 5. 문제 해결

| 증상 | 확인할 것 |
|---|---|
| 시트에 아무것도 안 쌓임, Logcat 에 `disabled` | `local.properties` 의 `METRIC_LOG_URL / METRIC_LOG_TOKEN` 누락 → 추가 후 Gradle Sync |
| Logcat `cause=Metric logging rejected by server: unauthorized` | 앱 `METRIC_LOG_TOKEN` ≠ 스크립트 속성 `TOKEN` |
| `cause=... unknown event: xxx` | 시트 탭 이름 ≠ `MetricLogging.name` |
| curl 은 되는데 앱만 실패 | URL 을 `local.properties` 에 넣은 뒤 재빌드했는지, 값 앞뒤 공백/따옴표가 없는지 |
| 스크립트를 고쳤는데 동작이 그대로 | `배포 관리 → 새 버전` 으로 재배포했는지 (2.5) |
| `timestamp` 시간대가 이상함 | Apps Script 프로젝트 시간대 (2.4) |
| 값이 엉뚱한 컬럼에 들어감 | 헤더 오타. 매핑은 **헤더 이름** 기준이며 위치는 무관 |

Apps Script 쪽 실행 로그는 편집기 좌측 `실행` 메뉴에서 볼 수 있습니다 (요청별 성공/예외 기록).

---

## 6. 운영 시 참고

- **보안 수준**: URL + TOKEN 을 아는 누구나 행을 추가할 수 있습니다. 릴리즈 앱에 넣으면 APK 에서 추출될 수 있으므로, 이 방식은 **학습·프로토타입·내부 테스트용**으로 적합합니다. 실서비스에서는 `MetricLoggingRepository` 구현을 Firebase Analytics / 자체 서버 등으로 교체하세요 — domain 인터페이스는 그대로입니다.
- **쿼터**: Apps Script 웹 앱은 일일 실행 시간/횟수 제한이 있습니다. 클릭 단위 이벤트를 소규모로 쌓는 용도에는 충분하지만, 고빈도(스크롤·프레임 단위) 이벤트에는 맞지 않습니다.
- **오프라인**: 현재 구현은 재시도/큐가 없습니다. 네트워크가 없으면 그 이벤트는 유실됩니다.
