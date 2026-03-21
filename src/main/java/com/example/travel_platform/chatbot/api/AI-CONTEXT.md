<!-- Parent: ../AI-CONTEXT.md -->

# api

## 목적

챗봇 질문 요청을 받는 REST API 엔드포인트를 둔다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| ChatbotController.java | 챗봇 질문 API 진입점이다. |

## 하위 디렉토리

- `dto/` - 요청/응답 DTO를 둔다.

## AI 작업 지침

- 요청/응답 스키마를 바꾸면 DTO와 테스트를 함께 갱신한다.
- API 경로 변경 시 프런트엔드 챗봇 호출부도 같이 확인한다.
- `ChatbotController`는 현재 `/api/chatbot/messages` 단건 질문 입구만 담당하고, orchestrator 위임만 수행한다.
- `ChatbotController`는 `Resp.ok(...)`로 감싼 API 응답만 반환하고, 내부 상태 객체를 직접 노출하지 않는다.
- `ChatbotRequest`, `ChatbotResponse`는 `createAskRequest`, `createContext`, `createAskResponse`, `createDirectMeta`, `createDbMeta` factory 이름을 유지한다.

## 테스트

- `/api/chatbot/messages` 요청과 응답 형태, validation 실패, JSON 파싱 실패, service 예외 응답을 검증한다.

## 의존성

- 내부: `dto`, `application`
- 외부: `Spring MVC`, `Jakarta Validation`

