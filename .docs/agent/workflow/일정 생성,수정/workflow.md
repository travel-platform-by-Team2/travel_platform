# TASK02 - 캘린더 일정 생성/수정 화면 연동

## 0. 작업 유형

- 기능 추가

## 1. 작업 목표

- 캘린더 화면에서 일정 생성/수정이 동작하도록 `CalendarApiController`에 `createEvent`, `updateEvent`를 연결한다.
- 사용자 인증/검증 단계는 제거하고 화면에 일정 추가/표시만 확인한다.
- 계층 구조(`CalendarApiController -> CalendarService -> CalendarRepository`)를 유지한다.

## 2. 작업 범위

- `CalendarApiController`에 `createEvent`, `updateEvent` 추가 및 화면 연동
- `CalendarService` 생성/수정 메서드 구현
- `CalendarRepository` 저장/수정 처리 연결
- 화면 반영을 위한 응답 DTO 매핑

## 3. 작업 제외 범위

- Entity/DB schema 변경
- 공개 API 스펙 변경
- 인증/권한 로직 추가
- 캘린더 목록 조회 로직 개선

## 4. 완료 기준

- [x] 화면에서 일정 생성 요청 시 일정이 저장되고 화면에 표시된다.
- [x] 화면에서 일정 수정 요청 시 수정된 일정이 화면에 표시된다.
- [x] Controller -> Service -> Repository 계층을 위반하지 않는다.

## 5. 예상 영향 범위

- 대상 레이어: calendar controller/service/repository
- 대상 기능: 일정 생성/수정 및 화면 반영
- 사용자 영향: 캘린더 화면에서 일정 추가/수정 가능

## 6. 위험도

- 등급: MEDIUM
- 근거: 인증/검증 제거로 인해 데이터 무결성 및 보안 영향 가능

## 7. 승인 필요 여부

- 필요 여부: 예
- 승인 필요 사유: 인증/권한 로직 변경(제거)

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표                   | 입력                                                   | 출력                   | 검증                                  |
| ---- | ---------------------- | ------------------------------------------------------ | ---------------------- | ------------------------------------- |
| 1    | 현재 API/DTO 구조 확인 | controller/service/repository 시그니처, 화면 요청 형식 | 구현 대상 메서드 목록  | 시그니처 불일치 여부 확인             |
| 2    | Controller 연결        | createEvent/updateEvent 요청 DTO                       | controller 메서드 추가 | 라우팅 경로 및 DTO 매핑 확인          |
| 3    | Service 로직 구현      | DTO, 엔티티 필드 규칙                                  | 생성/수정 처리 흐름    | 인증/검증 로직 미포함 확인            |
| 4    | Repository 저장 처리   | 엔티티, 저장 규칙                                      | save/update 호출 흐름  | DB 스키마 변경 없음 확인              |
| 5    | 화면 반영 확인         | 생성/수정 응답 DTO                                     | 화면 반영 흐름         | 생성/수정 결과가 화면에 보이는지 확인 |

### 8.2 중단 조건

- Entity/DB schema 변경이 필요해지는 경우
- 공개 API 스펙 변경이 필요한 경우
- 변경 파일 5개 이상 또는 diff 200줄 이상으로 확대되는 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`
- 테스트 명령: `./gradlew test --tests *Calendar*` (가능 시)
- 수동 검증 항목:
  - 캘린더 화면에서 일정 추가 후 표시 확인
  - 캘린더 화면에서 일정 수정 후 표시 확인

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/calendar/CalendarApiController.java`
  - `src/main/java/com/example/travel_platform/calendar/CalendarService.java`
  - `src/main/resources/templates/pages/calendar.mustache`
  - `src/main/resources/static/js/calendar-add-event.js`
- 테스트 결과: 미실행
- 미완료 항목(TODO): 없음
- 리스크/주의사항: 인증/권한 검증 제거 상태이므로 운영 적용 전 복구 필요
