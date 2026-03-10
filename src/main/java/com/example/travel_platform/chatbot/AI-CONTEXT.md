<!-- Parent: ../AI-CONTEXT.md -->

# chatbot

## 목적

여행 보조 챗봇 기능의 API, 애플리케이션 서비스, LLM 연동 구현을 묶는다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| 없음 | 이 디렉토리는 하위 챗봇 패키지를 묶는 루트다. |

## 하위 디렉토리

- `api/` - 챗봇 REST API 입구를 둔다.
- `application/` - 챗봇 유스케이스 서비스를 둔다.
- `infra/` - 외부 LLM 연동 구현체를 둔다.

## AI 작업 지침

- 현재 흐름은 계획 생성 -> 안전한 SQL 실행/LLM 재평가 반복 -> 최종 답변 생성 구조다.
- DB 조회형 질문은 `LLM 자율 판단 + 최대 5회 하드캡`으로 재탐색할 수 있으므로, 탐색 이력 누적 형식을 함께 유지한다.
- 허용 테이블과 SQL 안전성 정책은 `ChatbotQueryService`를 기준으로 유지한다.

## 테스트

- 챗봇 관련 변경 후에는 API, 오케스트레이션, SQL 안전성 테스트를 같이 확인한다.

## 의존성

- 내부: `api`, `application`, `infra`
- 외부: `Spring JDBC`, `Gson`, `OpenAI Responses API`
