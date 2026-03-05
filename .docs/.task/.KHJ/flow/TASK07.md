# TASK07 - 서비스 계층 컨벤션 1차 정리

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- 서비스 계층의 트랜잭션 선언 방식을 코드 컨벤션에 맞춘다.
- 명확하지 않은 메서드 네이밍(예: 한글 메서드명)을 camelCase 기반으로 정리한다.

## 2. 작업 범위

- `trip/community/calendar/booking/user` 서비스 클래스에 클래스 레벨 `@Transactional(readOnly = true)` 적용
- 쓰기 메서드에 메서드 레벨 `@Transactional` 유지
- `UserService`의 한글 메서드명을 camelCase로 리네이밍
- `UserController` 호출부 동기화
- 문서/작업 로그 업데이트

## 3. 작업 제외 범위

- 서비스 비즈니스 로직 구현 확장
- 리포지토리 구조 전환(JpaRepository 인터페이스화)
- API 응답 스펙 변경

## 4. 완료 기준

- [x] 대상 서비스 클래스가 `@Transactional(readOnly = true)`를 가진다.
- [x] 쓰기 메서드가 메서드 레벨 `@Transactional`을 가진다.
- [x] `UserService` 한글 메서드명이 camelCase로 교체된다.
- [x] `./gradlew compileJava` 성공

## 5. 예상 영향 범위

- 대상 레이어: service, user controller
- 대상 기능: 트랜잭션 경계/메서드 호출
- 사용자 영향: 없음(내부 구조/가독성 개선)

## 6. 위험도

- 등급: LOW
- 근거: 로직 변경 없이 선언/이름 정리 중심

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API/DB/설정 변경 없음

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 트랜잭션 선언 정리 | 기존 서비스 클래스 | 클래스/메서드 트랜잭션 정리본 | 컴파일 |
| 2 | 메서드명 정리 | `UserService`, `UserController` | camelCase 이름/호출부 동기화 | 컴파일 |
| 3 | 문서 동기화 | 변경 파일/검증 결과 | TASK/WORKLOG 갱신 | 문서 점검 |

### 8.2 중단 조건

- 이름 변경으로 범위 외 API/뷰 계약 수정이 필요한 경우
- 예상치 못한 광범위 참조 파손 발생 시

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/trip/TripService.java`
  - `src/main/java/com/example/travel_platform/community/CommunityService.java`
  - `src/main/java/com/example/travel_platform/calendar/CalendarService.java`
  - `src/main/java/com/example/travel_platform/booking/BookingService.java`
  - `src/main/java/com/example/travel_platform/user/UserService.java`
  - `src/main/java/com/example/travel_platform/user/UserController.java`
  - `.docs/.task/.KHJ/flow/TASK07.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- 테스트 결과:
  - `./gradlew compileJava` 성공
- 미완료 항목(TODO):
  - 리포지토리 구조 컨벤션 정렬(인터페이스/JpaRepository 기준) 검토
  - REST 응답 `Resp` 래퍼 적용 범위 정리
- 리스크/주의사항:
  - 메서드명 변경(`join`, `login`)으로 직접 호출 코드가 있다면 동기화 필요(현재 프로젝트 내 반영 완료)
