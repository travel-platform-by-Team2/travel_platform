<!-- Parent: ../AI-CONTEXT.md -->

# calendar

## 목적

캘린더 SSR 화면과 일정 CRUD API, 일정 범위 조회 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `CalendarController.java` | 캘린더 SSR 화면 진입 |
| `CalendarApiController.java` | 일정 생성/수정/삭제/조회 API |
| `CalendarService.java` | 일정 CRUD, 범위 조회 |
| `CalendarRequest.java` | 일정 생성/수정 입력 DTO |
| `CalendarResponse.java` | 캘린더 화면 DTO, 일정 DTO |
| `CalendarRepository.java` | 저장, 수정, 삭제, 단건 조회 |
| `CalendarQueryRepository.java` | 일정 범위 JPQL 조회 |

## 현재 구조 기준

- SSR 모델 키는 `model` 단건 규칙을 사용한다.
- `calendar.mustache`는 `model.pageTitle`을 사용한다.
- `CalendarController`는 화면 진입 시 `CalendarPageDTO`만 넣고 렌더링한다.
- `CalendarApiController`는 `sessionUser` 기준 사용자 ID를 꺼내고 `Resp.ok(...)`만 반환한다.
- `CalendarService`는 생성, 수정, 삭제, 범위 조회를 분리한다.
- 일정 수정/삭제는 `sessionUser` 기준 소유자 검증을 통과해야 한다.
- `CalendarResponse.EventDTO`는 `fromCalendarEvent(...)` 이름 규칙을 사용한다.
- `CalendarApiController`의 조회 API는 `startDate`, `endDate` 범위 조회만 지원한다.
- `CalendarEvent.eventType`은 `CalendarEventType` enum + converter 기준으로 저장하고, API/SSR 응답에서는 문자열 코드만 노출한다.
- `CalendarRequest.CreateEventDTO`의 `tripPlanId`가 있으면 create 흐름에서 `TripPlan`을 연결한다.

## 정규화 메모

- `CalendarEvent`는 `User`, `TripPlan`을 함께 참조하므로 여행 계획과 개인 일정이 섞이는 구조다.
- `eventType` 문자열 직접 저장은 제거하고 `CalendarEventType` enum 저장으로 전환했다.
- `memo`는 엔티티/SQL 스키마 기준을 맞춰 nullable 텍스트로 유지한다.
- 사용하지 않는 `day node` 조회 구조와 DTO는 제거했다.

## 테스트

- `CalendarControllerTest`
- `CalendarApiControllerTest`
- `CalendarServiceTest`
- `CalendarEventTypeTest`
- `CalendarResponseTest`
- `CalendarTemplateContractTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.calendar.CalendarServiceTest --tests com.example.travel_platform.calendar.CalendarEventTypeTest --tests com.example.travel_platform.calendar.CalendarResponseTest --tests com.example.travel_platform.calendar.CalendarApiControllerTest --tests com.example.travel_platform.calendar.CalendarControllerTest --tests com.example.travel_platform.calendar.CalendarTemplateContractTest`와 `./gradlew.bat test` 기준으로 맞춘다.
