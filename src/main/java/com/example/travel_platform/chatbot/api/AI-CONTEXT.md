<!-- Parent: ../AI-CONTEXT.md -->

# api

## 목적

챗봇 질문 요청을 받는 REST API 계층이다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `ChatbotApiController.java` | 로그인 사용자의 챗봇 요청을 받아 `ChatbotService`로 위임한다. |

## 하위 디렉토리

- `dto/` - 요청/응답 DTO

## AI 작업 지침

- 경로는 `/api/chatbot/messages`를 유지한다.
- 컨트롤러는 로그인 확인과 요청 위임만 담당한다.
- API 응답은 `Resp.ok(...)` 래퍼를 사용한다.
