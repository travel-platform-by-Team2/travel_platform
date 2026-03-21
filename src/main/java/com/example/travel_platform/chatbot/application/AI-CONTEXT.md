<!-- Parent: ../AI-CONTEXT.md -->

# application

## 목적

LLM 계획 생성, SQL 실행, 재탐색 판단, 최종 답변 조합 등 챗봇 유스케이스를 구현한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| ChatbotAnswerService.java | 직접 응답과 탐색 이력 기반 최종 답변 생성을 처리한다. |
| ChatbotOrchestrator.java | 챗봇 전체 처리 흐름과 최대 5회 재탐색 루프를 오케스트레이션한다. |
| ChatbotPlanService.java | LLM 계획 생성과 재탐색 판단을 담당한다. |
| ChatbotQueryService.java | SQL 안전성 검증과 실제 조회를 담당한다. |
| ChatSchemaProvider.java | 허용 스키마 컨텍스트를 제공한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `ChatbotOrchestrator`의 단계 분리를 유지하고 한 서비스에 여러 책임을 몰아넣지 않는다.
- DB 조회형 질문은 `계획 -> 조회 -> 재평가` 반복 후 답변으로 끝나는 흐름을 유지하고, 재탐색 판단을 룰 기반으로 고정하지 않는다.
- SQL 안전성 검사는 우회하지 말고 허용 테이블/키워드 정책을 명시적으로 조정한다.
- `ChatbotOrchestrator`의 최대 5회 하드캡과 `ApiException` 오류 경계는 유지하고, direct answer와 DB query 분기를 섞지 않는다.
- `ChatbotOrchestrator`는 `DbSearchState` 기준으로 query intent, query summary, sql 상태를 넘기며 direct/db 응답 조립은 helper로 분리돼 있다.
- `ChatSchemaProvider`는 실제 컬럼 기준 schemaContext를 제공하고, `ChatbotPlanService`는 그 문자열을 그대로 LLM client에 전달한다.
- `ChatbotAnswerService`는 direct answer 기본값과 DB answer 기본 intent 보정을 담당한다.
- v3 기준으로 동적 SQL은 `ChatbotQueryService`만 예외적으로 담당하고, 다른 application 서비스는 SQL 문자열을 직접 다루지 않는다.

## 테스트

- 계획 생성, schema context 전달, direct answer 기본값, SQL 안전성, 재탐색 오케스트레이션 결과를 각각 검증한다.
- direct answer 분기와 DB query 분기 메타데이터(`processingType`, `meta`)를 같이 확인한다.

## 의존성

- 내부: `chatbot/api/dto`, `chatbot/infra/llm`
- 외부: `Spring JDBC`, `OpenAI Responses API`

