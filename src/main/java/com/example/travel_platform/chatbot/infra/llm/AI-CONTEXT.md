<!-- Parent: ../AI-CONTEXT.md -->

# llm

## 목적

OpenAI Responses API 기반 챗봇 LLM 클라이언트와 계획 모델을 둔다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| ChatbotLlmClient.java | LLM 클라이언트 인터페이스다. |
| ChatbotLlmPlan.java | LLM 계획 결과 모델이다. |
| OpenAiChatbotLlmClient.java | OpenAI Responses API 호출과 응답 파싱 구현체다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `OPENAI_API_KEY`, 모델명, 엔드포인트는 설정으로 주입받는 현재 방식을 유지한다.
- 응답 파싱은 JSON 코드펜스와 `output_text`/`output[].content[]` 둘 다 처리하므로 변경 시 회귀 테스트를 추가한다.

## 테스트

- 응답 포맷 변경 시 `ChatbotLlmPlan` 관련 테스트와 전체 챗봇 흐름을 함께 확인한다.

## 의존성

- 내부: `chatbot/application`, `_core/handler/ex`
- 외부: `HttpURLConnection`, `Gson`, `OpenAI Responses API`
