<!-- Parent: ../AI-CONTEXT.md -->

# dto

## 목적

챗봇 API 요청/응답 DTO를 정의한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `ChatbotRequest.java` | 메시지와 선택적 페이지 컨텍스트를 받는다. |
| `ChatbotResponse.java` | 답변, 처리 모드, 사용 조회 도메인, 데이터 충분 여부를 응답한다. |

## AI 작업 지침

- `ChatbotRequest`는 `message`, `context.page`, `context.tripPlanId` 구조를 유지한다.
- `ChatbotResponse`는 최소한 `answer`, `mode`, `usedTools`, `hasSufficientData`를 포함한다.
- 프런트 호환을 위해 `processingType`은 `mode`와 같은 값으로 유지한다.
