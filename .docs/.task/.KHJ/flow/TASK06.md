# TASK06 - 컨트롤러 구조/컨벤션 1차 리팩토링

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- 컨트롤러 구조를 코드 컨벤션에 맞게 정리한다.
- REST 컨트롤러 네이밍(`{Domain}ApiController`)과 API 경로(`/api` 접두사)를 맞춘다.
- Booking 도메인의 SSR/REST 혼합 컨트롤러를 분리한다.

## 2. 작업 범위

- `TripController` -> `TripApiController` 전환 및 `/api/trip-plans` 적용
- `CommunityController` -> `CommunityApiController` 전환 및 `/api/community/posts` 적용
- `CalendarController` -> `CalendarApiController` 전환 및 `/api/calendar/events` 적용
- `BookingController`에서 REST 메서드 분리 후 `BookingApiController` 신설
- 프론트 JS의 Booking API 호출 경로를 `/api/bookings/...`로 동기화
- 문서/작업 로그 업데이트

## 3. 작업 제외 범위

- 서비스/리포지토리 구현 로직 확장
- 인증/권한 정책 변경
- SQL 보안 정책 반영
- 엔티티/DB 스키마 변경

## 4. 완료 기준

- [x] REST 컨트롤러가 `{Domain}ApiController` 이름으로 정리된다.
- [x] REST 엔드포인트 경로에 `/api` 접두사가 적용된다.
- [x] Booking 도메인에서 SSR/REST가 파일로 분리된다.
- [x] 관련 프론트 API 호출 경로가 동기화된다.
- [x] `./gradlew compileJava`가 성공한다.

## 5. 예상 영향 범위

- 대상 레이어: controller, static js
- 대상 기능: 도메인 API 라우팅, 지도 페이지의 API 호출
- 사용자 영향: API 경로 정리(내부 구조 일관성 향상)

## 6. 위험도

- 등급: MEDIUM
- 근거: 다수 컨트롤러 파일 리네이밍 및 경로 변경 포함

## 7. 승인 필요 여부

- 필요 여부: 예(완료)
- 승인 필요 사유: 대규모 변경(파일 다수)이며 라우팅 경로 수정 포함

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | REST 컨트롤러 정리 | 기존 컨트롤러 3개 | ApiController 3개 + `/api` 경로 | 컴파일 |
| 2 | Booking 컨트롤러 분리 | 기존 `BookingController` | `BookingController`(SSR), `BookingApiController`(REST) | 컴파일 |
| 3 | 프론트 경로 동기화 | `map-detail.js` | `/api/bookings` 호출 코드 | 수동 경로 점검 |
| 4 | 문서 동기화 | 변경/검증 결과 | TASK/WORKLOG 기록 | 문서 점검 |

### 8.2 중단 조건

- 기존 페이지 라우팅이 치명적으로 깨지는 경우
- 범위 외 스펙 변경이 필요한 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`
- 선택 테스트:
  - `./gradlew test --tests "*ChatbotControllerTest"`
  - `./gradlew test --tests "*ChatbotServiceTest"`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/trip/TripApiController.java` (추가)
  - `src/main/java/com/example/travel_platform/trip/TripController.java` (삭제)
  - `src/main/java/com/example/travel_platform/community/CommunityApiController.java` (추가)
  - `src/main/java/com/example/travel_platform/community/CommunityController.java` (삭제)
  - `src/main/java/com/example/travel_platform/calendar/CalendarApiController.java` (추가)
  - `src/main/java/com/example/travel_platform/calendar/CalendarController.java` (삭제)
  - `src/main/java/com/example/travel_platform/booking/BookingApiController.java` (추가)
  - `src/main/java/com/example/travel_platform/booking/BookingController.java` (수정, SSR 전용화)
  - `src/main/resources/static/js/map-detail.js` (API 경로 수정)
  - `.docs/.task/.KHJ/flow/TASK06.md` (결과 기록)
  - `.docs/.task/.KHJ/WORKLOG.md` (이력 기록)
- 테스트 결과:
  - `./gradlew compileJava` 성공
  - `./gradlew test --tests "*ChatbotControllerTest"` 성공
  - `./gradlew test --tests "*ChatbotServiceTest"` 성공
- 미완료 항목(TODO):
  - 리포지토리/서비스 계층 컨벤션 2차 리팩토링
  - 전체 테스트(`./gradlew test`) 안정화
- 리스크/주의사항:
  - REST 경로가 `/api`로 정리되면서 기존 비표준 경로 호출은 더 이상 사용하지 않음
