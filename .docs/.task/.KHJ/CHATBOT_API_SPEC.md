# 챗봇 API 스펙 초안

## 0. 문서 정보
- 버전: `v0.2-draft`
- 작성일: `2026-03-04`
- 기준: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 상태: 설계 초안(구현 전)

## 1. 목표
챗봇 질문 1건을 처리해 자연어 답변을 반환하는 최소 API 계약을 정의한다.

## 2. 범위
- 포함: 질문 처리 API, LLM 오케스트레이션 규칙(DB 필요 여부 분기 포함)
- 제외: 대화 이력 저장/조회, DB 테이블 설계, 권한 정책 상세

## 3. 공통 규칙

### 3.1 콘텐츠 타입
- 요청: `application/json`
- 응답: `application/json`

### 3.2 인증/필터
- 현재 단계: 인증/인가 미적용(개발 우선)
- 챗봇 API는 당분간 인증 없이 동작
- 필터 적용 경로는 추후 확정

### 3.3 상태 관리 원칙(비저장)
- 서버는 대화/메시지 이력을 저장하지 않는다.
- 챗봇 메시지는 프론트 화면에서만 표시한다.
- 페이지 새로고침 시 대화 내용은 전부 초기화된다.
- 대화 이력 조회 API는 제공하지 않는다.

### 3.4 현재 개발 원칙(보안 후순위)
- 기능 구현 속도를 우선한다.
- LLM이 생성한 SQL은 서버가 그대로 실행하는 흐름으로 구현한다.
- SQL 검증/화이트리스트/권한 제약 등 보안 강화는 마무리 단계에서 반영한다.

## 4. 핵심 동작 원리 (확정)

1. 사용자가 자연어 질문 전송
2. LLM이 질문을 1차 분류
   - `DB 데이터 불필요`: LLM이 즉시 답변 생성 후 종료
   - `DB 데이터 필요`: LLM이 조회용 SQL 생성
3. 서버가 SQL로 DB 조회
4. 서버가 `원 질문 + 조회 결과`를 LLM에 재전달
5. LLM이 최종 자연어 답변 생성 후 사용자에게 반환

## 5. 오케스트레이션 계약 (내부)

### 5.1 분류 단계 출력(JSON)
```json
{
  "needsDb": true,
  "reason": "예약 목록 조회가 필요함",
  "queryIntent": "USER_BOOKING_LIST"
}
```

### 5.2 SQL 생성 단계 출력(JSON)
```json
{
  "sql": "select b.id, b.lodging_name, b.check_in, b.check_out from booking_tb b where b.user_id = 1 order by b.check_in desc",
  "summary": "사용자 예약 목록 조회"
}
```

### 5.3 최종 답변 생성 입력
서버가 LLM에 전달:
- 사용자 질문 원문
- SQL 실행 결과(JSON 배열, DB 필요 시)
- 화면 컨텍스트(선택)

## 6. API 상세

### 6.1 질문 처리
- Method/Path: `POST /api/chatbot/messages`
- 설명: 질문 1건을 처리하고 최종 답변을 반환

요청 바디:
```json
{
  "message": "제주도 2박 3일 추천해줘",
  "context": {
    "page": "trip-plan-detail",
    "tripPlanId": 1
  }
}
```

요청 필드:
| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `message` | String | Y | 사용자 입력 질문(1~1000자) |
| `context.page` | String | N | 호출 페이지 식별자 |
| `context.tripPlanId` | Integer | N | 화면 컨텍스트용 ID |

성공 응답 예시 1 (`DIRECT_LLM`):
```json
{
  "processingType": "DIRECT_LLM",
  "answer": "제주도 2박 3일 코스를 추천해드릴게요.",
  "meta": {
    "needsDb": false
  }
}
```

성공 응답 예시 2 (`DB_QUERY`):
```json
{
  "processingType": "DB_QUERY",
  "answer": "최근 예약 내역은 총 2건입니다.",
  "meta": {
    "needsDb": true,
    "querySummary": "사용자 예약 목록 조회",
    "generatedSql": "select b.id, b.lodging_name, b.check_in, b.check_out from booking_tb b where b.user_id = 1 order by b.check_in desc",
    "rowCount": 2
  }
}
```

응답 필드:
| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `processingType` | String | `DIRECT_LLM` 또는 `DB_QUERY` |
| `answer` | String | 사용자에게 표시할 최종 답변 |
| `meta.needsDb` | Boolean | DB 조회 필요 여부 |
| `meta.querySummary` | String | 실행 SQL 목적 요약(선택) |
| `meta.generatedSql` | String | LLM 생성 SQL(현재 개발 단계 디버깅용, 선택) |
| `meta.rowCount` | Integer | 조회 결과 행 수(선택) |

## 7. 에러 응답 규격(챗봇 API 전용 제안)

에러 응답 예시:
```json
{
  "code": "CHATBOT_BAD_REQUEST",
  "message": "message는 1자 이상이어야 합니다.",
  "status": 400,
  "timestamp": "2026-03-04T19:10:00"
}
```

에러 코드:
| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | `CHATBOT_BAD_REQUEST` | 입력값 누락/형식 오류 |
| 500 | `CHATBOT_INTERNAL_ERROR` | 내부 오류 |

비고:
- `401`, `403`, `404`는 인증/인가/리소스 정책 확정 시 추가한다.

## 8. 구현 시 확인사항
1. `GlobalExceptionHandler`는 현재 HTML Script 문자열 응답 기반이므로 JSON 응답 분리가 필요
2. `ValidationHandler`와 챗봇 요청 DTO 검증 규칙 연결 필요
3. 인증/필터 적용은 경로 정책 확정 후 후속 단계에서 반영
4. SQL 실행 보안 정책은 마무리 단계에서 별도 보완
5. 비저장 원칙 유지: 서버/DB에 대화 로그 저장 로직을 추가하지 않음

