<!-- Parent: ../AI-CONTEXT.md -->

# llm

## 목적

OpenAI Responses API 기반 질문 해석과 답변 생성을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `ChatbotLlmClient.java` | 질문 해석, 일반 대화 답변, DB 답변 합성 계약을 정의한다. |
| `OpenAiChatbotLlmClient.java` | OpenAI 호출과 JSON 파싱을 구현한다. |

## AI 작업 지침

- 현재 LLM 역할은 SQL 생성이 아니라 `질문 해석 + 자연어 답변 생성`이다.
- 질문 해석 응답은 JSON 객체로 강제한다.
- 허용 도메인 이름은 `BOOKING`, `TRIP`, `CALENDAR`, `BOARD`다.
- 실패 시 `_core` 예외 체계로 넘길 수 있도록 `ApiException`을 사용한다.
