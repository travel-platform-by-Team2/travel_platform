<!-- Parent: ../AI-CONTEXT.md -->

# application

## 목적

질문 분류, 챗봇 전용 조회, 답변 생성까지 챗봇 핵심 유스케이스를 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `ChatbotService.java` | 질문 해석, 일반 대화/DB 질의응답 분기, 응답 조합을 담당한다. |
| `ChatQueryRepository.java` | 예약/여행계획/캘린더/게시글 조회를 한 곳에서 수행한다. |

## AI 작업 지침

- 지금 구조는 확장형 파이프라인이 아니라 1차 범위에 맞춘 단순 서비스형 구조다.
- 질문 해석 결과 모델은 `ChatbotService` 내부 클래스로 관리한다.
- 조회 도메인은 `BOOKING`, `TRIP`, `CALENDAR`, `BOARD`만 지원한다.
- 개인 데이터는 `ChatQueryRepository`에서 `userId` 기준으로 바로 제한한다.
- 조회 결과가 비면 `ChatbotService`에서 재질문 유도 메시지로 정리한다.
