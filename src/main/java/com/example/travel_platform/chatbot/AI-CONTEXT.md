<!-- Parent: ../AI-CONTEXT.md -->

# chatbot

## 목적

로그인 사용자 전용 챗봇 도메인이다.
기본 흐름은 `LLM 질문 해석 -> 내부 조회/도구 호출 -> LLM 자연어 답변`이다.

## 주요 구조

- `ChatbotApiController -> ChatbotService -> (ChatQueryRepository, ChatbotLlmClient, WeatherService)`
- 일반 대화는 `GENERAL_CHAT`로 처리한다.
- 내부 데이터와 날씨는 모두 `DB_QA` 흐름 안에서 `QueryBlock`으로 만들고, 그 결과를 다시 LLM에 넘겨 답변을 만든다.

## 작업 메모

- 챗봇은 로그인 사용자만 사용할 수 있다.
- 조회 대상 도메인은 `BOOKING`, `TRIP`, `CALENDAR`, `BOARD`, `WEATHER`다.
- `WEATHER`는 LLM이 `keyword=region`, `startDate=targetDate`로 해석하면 `weather` 도메인의 공용 API 로직을 재사용한다.
- 브라우저에서는 현재 페이지 생명주기 동안만 대화 이력을 메모리에 유지하고, 다음 요청 때 함께 보낸다.
- 질문 해석 결과에는 `resolvedContext`가 포함되며, `domain`, `intent`, `region`, `targetDate`, `keyword`, `tripPlanId`, `isFollowUp`, `missingFields` 같은 공통 슬롯을 담는다.
- `resolvedContext`는 같은 도메인의 후속 질문에서 `queryPlan`의 빈 필드를 보완하는 데 사용한다.
- 날씨 응답도 다른 도메인과 같이 `QueryBlock`으로 만든 뒤 LLM이 최종 자연어 답변을 생성한다.

## 테스트

- 컨트롤러, DTO, `ChatbotService` 기준 테스트를 우선 확인한다.
