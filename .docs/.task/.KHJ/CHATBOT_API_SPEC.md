# 챗봇 API 스펙 (스토리보드 고정본)

## 0. 문서 정보
- 버전: `v0.3-draft`
- 작성일: `2026-03-05`
- 기준: 사용자 스토리보드(LLM 1차 판단 + DB 분기 + LLM 최종 응답)
- 상태: 구현 기준 문서(내부 계약 포함)

## 1. 목표
질문 1건 처리 시, 아래 플로우를 코드/문서에서 동일하게 재현할 수 있도록
외부 API 계약과 내부 LLM JSON 계약을 고정한다.

## 2. 범위
- 포함:
  - 외부 API `POST /api/chatbot/messages` 요청/응답
  - 내부 LLM 1차 계획(분기 + SQL) 계약
  - 내부 LLM 2차 답변 생성 계약
- 제외:
  - 인증/권한 상세 정책
  - SQL 보안 화이트리스트 상세 규격
  - 대화 저장/조회 API

## 3. 공통 규칙

### 3.1 콘텐츠 타입
- 요청: `application/json`
- 응답: `application/json`

### 3.2 상태 관리
- 서버는 대화 로그를 저장하지 않는다.
- 페이지 새로고침 시 대화 내용은 초기화된다.

### 3.3 인증/필터
- 현재 단계: 인증/인가 미적용
- 필터 경로 정책은 후속 확정

### 3.4 보안 정책(현 단계)
- 기능 구현 우선 단계이므로 LLM 생성 SQL을 서버가 실행한다.
- SQL 검증/권한 제약은 후속 단계에서 강화한다.

## 4. 스토리보드 기준 처리 흐름

1. 사용자가 자연어 질문 입력
2. 프론트가 스프링 서버로 전달
3. 서버가 LLM에 질문/컨텍스트 전달 (1차 계획 요청)
4. LLM이 `needsDb=false` 판단 시
   - LLM이 직접 최종 답변 생성
   - 서버가 사용자에게 즉시 전달
5. LLM이 `needsDb=true` 판단 시
   - 서버가 스키마 맥락(엔티티/테이블/컬럼) 포함해 SQL 계획 수신
   - 서버가 SQL 실행 후 조회 결과 획득
   - 서버가 질문 + 조회결과를 LLM에 재전달 (2차 답변 요청)
   - LLM이 자연어 답변 생성
   - 서버가 사용자에게 전달

## 5. 외부 API 계약

### 5.1 질문 처리 API
- Method/Path: `POST /api/chatbot/messages`
- 설명: 질문 1건 처리 후 최종 자연어 답변 반환

요청 바디:
```json
{
  "message": "내 최근 예약 보여줘",
  "context": {
    "page": "booking-list",
    "tripPlanId": 1
  }
}
```

요청 필드:
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `message` | String | Y | 사용자 질문 |
| `context.page` | String | N | 호출 화면 식별자 |
| `context.tripPlanId` | Integer | N | 화면 컨텍스트 ID |

성공 응답 예시 1 (`DIRECT_LLM`):
```json
{
  "processingType": "DIRECT_LLM",
  "answer": "최근 예약은 숙소 예약 메뉴에서 확인할 수 있어요.",
  "meta": {
    "needsDb": false
  }
}
```

성공 응답 예시 2 (`DB_QUERY`):
```json
{
  "processingType": "DB_QUERY",
  "answer": "최근 예약은 총 2건이며 가장 최근 숙소는 오션뷰 호텔입니다.",
  "meta": {
    "needsDb": true,
    "querySummary": "사용자 최근 예약 5건 조회",
    "generatedSql": "select b.id, b.lodging_name, b.check_in, b.check_out from booking_tb b order by b.check_in desc, b.id desc limit 5",
    "rowCount": 2
  }
}
```

응답 필드:
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `processingType` | String | `DIRECT_LLM` 또는 `DB_QUERY` |
| `answer` | String | 최종 사용자 응답 |
| `meta.needsDb` | Boolean | DB 조회 사용 여부 |
| `meta.querySummary` | String | SQL 목적 요약 (`DB_QUERY` 시 권장) |
| `meta.generatedSql` | String | 실행 SQL (`DB_QUERY` 시 권장) |
| `meta.rowCount` | Integer | 조회 결과 건수 (`DB_QUERY` 시 권장) |

## 6. 내부 LLM 계약(JSON)

## 6.1 1차 계획 요청 (Server -> LLM)
서버 입력 개념:
- 사용자 질문 원문
- 화면 컨텍스트
- 테이블/컬럼 스키마 정보
- 출력 JSON 스키마 규칙

LLM 응답 JSON 고정 스키마:
```json
{
  "needsDb": true,
  "queryIntent": "USER_BOOKING_LIST",
  "querySummary": "사용자 최근 예약 조회",
  "sql": "select b.id, b.lodging_name, b.check_in, b.check_out from booking_tb b order by b.check_in desc, b.id desc limit 5",
  "answer": ""
}
```

규칙:
- `needsDb=false`:
  - `answer` 필수
  - `sql`은 빈 문자열 허용
- `needsDb=true`:
  - `sql` 필수(SELECT만 허용)
  - `queryIntent`, `querySummary` 권장
  - `answer`는 빈 문자열 허용

## 6.2 DB 결과 기반 답변 요청 (Server -> LLM)
서버 요청 JSON 예시:
```json
{
  "userMessage": "내 최근 예약 보여줘",
  "queryIntent": "USER_BOOKING_LIST",
  "rows": [
    {
      "id": 10,
      "lodging_name": "오션뷰 호텔",
      "check_in": "2026-03-10",
      "check_out": "2026-03-12"
    },
    {
      "id": 9,
      "lodging_name": "시티 인",
      "check_in": "2026-02-01",
      "check_out": "2026-02-03"
    }
  ]
}
```

LLM 응답 JSON 예시:
```json
{
  "answer": "최근 예약은 총 2건이며 가장 최근 숙소는 오션뷰 호텔입니다."
}
```

규칙:
- rows가 비어있으면 데이터 없음 안내를 포함해야 한다.
- 답변은 사용자 표시용 자연어 문장으로 생성한다.

## 7. 에러 응답 규격

예시:
```json
{
  "code": "CHATBOT_BAD_REQUEST",
  "message": "message must not be blank",
  "status": 400,
  "timestamp": "2026-03-05T18:30:00"
}
```

에러 코드:
| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | `CHATBOT_BAD_REQUEST` | 입력 형식/검증 오류 |
| 500 | `CHATBOT_INTERNAL_ERROR` | LLM 호출/DB 조회/내부 처리 오류 |

## 8. 구현 체크포인트
1. `needsDb=false`에서는 DB를 호출하지 않는다.
2. `needsDb=true`인데 SQL이 비어있으면 내부 오류로 처리한다.
3. DB 조회 결과는 LLM 2차 호출 입력으로 그대로 전달한다.
4. 최종 사용자 응답은 항상 자연어 `answer` 문자열로 반환한다.
5. 대화 비저장 원칙을 유지한다.
