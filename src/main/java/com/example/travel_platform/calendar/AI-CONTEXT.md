<!-- Parent: ../AI-CONTEXT.md -->

# calendar

## 목적

여행 일정 캘린더의 화면, API, 엔티티, 저장소를 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| CalendarApiController.java | 일정 관련 REST API를 제공한다. |
| CalendarController.java | 캘린더 화면을 렌더링한다. |
| CalendarEvent.java | 일정 엔티티다. |
| CalendarRepository.java | 일정 저장/조회/삭제를 담당한다. |
| CalendarRequest.java | 일정 입력 DTO를 정의한다. |
| CalendarResponse.java | 일정 응답 DTO를 정의한다. |
| CalendarService.java | 일정 비즈니스 로직을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 세션 사용자가 없을 때 일부 API가 `userId=1`로 폴백하므로 인증 로직을 바꿀 때 함께 정리한다.
- 일별, 월별, 범위 조회는 응답 형태가 다르므로 프론트엔드 기대값을 깨지 않게 수정한다.

## 테스트

- 일정 생성, 수정, 삭제와 월별/일별 조회를 나눠서 확인한다.

## 의존성

- 내부: `user`, `src/main/resources/templates/pages/calendar.mustache`
- 외부: `Spring MVC`, `JPA/Hibernate`
