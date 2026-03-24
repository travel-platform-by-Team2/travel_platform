<!-- Parent: ../AI-CONTEXT.md -->

# chatbot

## 목적

로그인 사용자 전용 챗봇 도메인이다.
기본 흐름은 `LLM 질문 해석 -> 내부 조회/도구 호출 -> LLM 자연어 답변`이다.

## 주요 구조

- `ChatbotApiController -> ChatbotService -> (ChatQueryRepository, ChatbotLlmClient, WeatherService)`
- 일반 대화는 `GENERAL_CHAT`로 답한다.
- 내부 데이터와 날씨는 모두 `DB_QA` 흐름 안에서 `QueryBlock`으로 만들고, 그 결과를 다시 LLM에 넘겨 답한다.

## 작업 메모

- 챗봇은 로그인 사용자만 사용할 수 있다.
- 조회 대상 도메인은 `BOOKING`, `TRIP`, `CALENDAR`, `BOARD`, `WEATHER`다.
- `WEATHER`는 LLM이 `keyword=region`, `startDate=targetDate`로 해석하면 `weather` 도메인의 공용 API 로직을 재사용한다.
- 날씨 응답도 다른 도메인과 같이 `QueryBlock`으로 만든 뒤 LLM이 최종 자연어 답변을 생성한다.
