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

- `CalendarController`는 현재 SSR 화면 진입만 담당하고, 서비스 의존성 없이 `pages/calendar` 뷰만 반환한다.
- `CalendarApiController`는 세션 사용자 ID를 직접 해석하며, 비로그인 요청은 보통 `LoginInterceptor`에서 먼저 차단된다.
- `calendar.mustache`는 `{{>partials/chatbot-assets}}`와 `/js/calendar-add-event.js`를 직접 포함하고, 공통 `scripts.mustache`는 더 이상 캘린더 전용 JS를 싣지 않는다.
- 생성, 수정 시 시작일이 종료일보다 늦지 않도록 공통 검증하고, 수정 시 이벤트에 사용자 정보가 없으면 현재 사용자로 보정한다.
- 조회 분기는 `date -> day`, `year + month -> month`, 그 외 -> range 순서로 처리하므로 프론트엔드 기대값을 깨지 않게 수정한다.
- `getDayNodeList`, `getDayNode`는 아직 placeholder 성격이 남아 있으므로 월/일 상세 기능을 확장할 때 서비스 구현과 프런트 기대값을 같이 맞춘다.
- `CalendarResponse.EventDTO.from(...)`이 이벤트 응답 변환 기준이므로 서비스에서 직접 builder를 반복하지 않는다.

## 테스트

- `CalendarControllerTest`, `CalendarApiControllerTest`, `CalendarServiceTest`로 생성, 수정, 삭제와 range, month, day, 비로그인 예외를 나눠서 확인한다.
- `CalendarTemplateContractTest`와 `StaticScriptContractTest`로 캘린더 화면의 template/script 계약을 확인한다.
- month, day 조회는 아직 placeholder 상태이므로 `빈 리스트`, `null` 계약을 유지하는지 함께 본다.

## 의존성

- 내부: `user`, `src/main/resources/templates/pages/calendar.mustache`
- 외부: `Spring MVC`, `JPA/Hibernate`
