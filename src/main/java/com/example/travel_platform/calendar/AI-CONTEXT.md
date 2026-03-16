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

- `CalendarApiController`는 세션 사용자 ID를 직접 해석하며, 비로그인 요청은 보통 `LoginFilter`에서 먼저 차단된다.
- 생성/수정 시 시작일이 종료일보다 늦지 않도록 검증하고, 수정 시 이벤트에 사용자 정보가 없으면 현재 사용자로 보정한다.
- 일별, 월별, 범위 조회는 응답 형태가 다르므로 프론트엔드 기대값을 깨지 않게 수정한다.
- `getDayNodeList`, `getDayNode`는 아직 placeholder 성격이 남아 있으므로 월/일 상세 기능을 확장할 때 서비스 구현과 프런트 기대값을 같이 맞춘다.

## 테스트

- 일정 생성, 수정, 삭제와 범위/월별/일별 조회를 나눠서 확인한다.
- 비로그인 상태에서는 필터 401/redirect가 유지되는지, 로그인 상태에서는 세션 사용자 기준 데이터만 조회되는지 함께 본다.

## 의존성

- 내부: `user`, `src/main/resources/templates/pages/calendar.mustache`
- 외부: `Spring MVC`, `JPA/Hibernate`
