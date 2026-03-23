<!-- Parent: ../AI-CONTEXT.md -->

# chatbot

## 목적

로그인 사용자 전용 챗봇 기능을 담당한다. 1차 목표는 DB 기반 질의응답이며, 질문 해석과 답변 생성은 LLM이 맡고 실제 조회는 챗봇 전용 JPA 조회가 수행한다.

## 하위 디렉토리

- `api/` - 챗봇 REST API 진입점과 요청/응답 DTO
- `application/` - 챗봇 서비스와 챗봇 전용 조회
- `infra/` - OpenAI 연동

## AI 작업 지침

- 현재 핵심 구조는 `ChatbotApiController -> ChatbotService -> (ChatQueryRepository, ChatbotLlmClient)`다.
- 챗봇은 로그인 사용자만 사용할 수 있으며, 컨트롤러에서 `SessionUsers.requireUserId(...)`로 인증을 강제한다.
- DB 조회는 JDBC를 쓰지 않고 챗봇 전용 JPA 조회만 사용한다.
- 조회 도메인은 `BOOKING`, `TRIP`, `CALENDAR`, `BOARD` 네 종류만 지원한다.
- 사용자에게는 자연어 답변만 노출하고 내부 해석/조회 흐름은 로그로 남긴다.

## 테스트

- 컨트롤러, DTO, `ChatbotService` 기준 테스트를 우선 확인한다.
