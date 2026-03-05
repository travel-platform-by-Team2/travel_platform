# TASK01 - 캘린더 일정 목록 조회 메서드 구현(현재 프로젝트 적용 상세)

## 0. 작업 유형

- 기능 추가

## 1. 작업 목표

- 현재 프로젝트의 캘린더 API(`GET /api/calendar`)에서 일정 목록 조회가 실제 DB 데이터를 반환하도록 구현한다.
- `CalendarApiController -> CalendarService -> CalendarRepository` 계층 구조를 유지한 채, 기간 기반 조회(`startDate`, `endDate`)와 기본 조회 동작을 완성한다.

## 2. 작업 범위

- `CalendarRepository.findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate)` 구현
- `CalendarService.getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate)` 구현
- 필요 시 `CalendarApiController.getCalendar(...)`의 목록 조회 분기 반환 타입 점검
- 목록 조회 구현 검증(컴파일/테스트 또는 실패 사유 기록)

## 3. 작업 제외 범위

- `CalendarEvent` Entity 필드/연관관계 변경
- DB schema 변경
- 인증/권한 정책 변경(세션 체크 방식 유지)
- 월별/일별 노드(`getDayNodeList`, `getDayNode`) 구현
- 일정 생성/수정/삭제 로직 구현

## 4. 완료 기준

- [ ] `GET /api/calendar` 호출 시 `List<CalendarResponse.EventDTO>`가 반환된다.
- [ ] `startDate`, `endDate`가 없으면 사용자 전체 일정이 시작일 오름차순으로 반환된다.
- [ ] `startDate`, `endDate`가 있으면 기간과 겹치는 일정만 반환된다.
- [ ] Controller -> Service -> Repository 계층 규칙을 위반하지 않는다.
- [ ] 검증 결과(명령/성공·실패/원인)가 문서에 기록된다.

## 5. 예상 영향 범위

- 대상 레이어: Calendar API, Service, Repository
- 대상 기능: 캘린더 일정 목록 조회
- 사용자 영향: 캘린더 화면 데이터 표시 정확도 향상

## 6. 위험도

- 등급: MEDIUM
- 근거: 조회 쿼리 조건과 DTO 매핑 오류 시 빈 목록/누락/정렬 이슈가 발생할 수 있음

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: Entity/DB schema/API 스펙 변경 없이 기존 시그니처 범위 내 구현

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 현 코드 계약 확인 | `CalendarApiController`, `CalendarService`, `CalendarRepository`, `CalendarResponse`, `CalendarEvent` | 구현 대상 메서드/필드 매핑표 | 시그니처 불일치 여부 확인 |
| 2 | Repository 조회 쿼리 구현 | userId/startDate/endDate, Entity 필드(`startAt`,`endAt`) | 기간 조건 포함 JPQL 쿼리 | 기간 null/존재 케이스 모두 커버 확인 |
| 3 | Service 매핑 로직 구현 | Repository 결과(List<CalendarEvent>) | `List<EventDTO>` 변환 코드 | `id/tripPlanId/title/startAt/endAt/eventType` 누락 없는지 확인 |
| 4 | Controller 분기 점검 | `getCalendar(...)` 분기 로직 | 목록 조회 분기 정상 동작 | date/year/month 우선순위 유지 확인 |
| 5 | 정적 점검/정리 | 변경 코드 | 경고 없는 최종 코드 | 미사용 import, Object 반환 남음 여부 확인 |
| 6 | 검증 및 기록 | 빌드/테스트 명령 결과 | TASK 결과 기록, WORKLOG 기록 | 명령/결과/실패 사유 누락 없음 |

### 8.2 구현 순서(파일별)

1. `src/main/java/com/example/travel_platform/calendar/CalendarRepository.java`
- 메서드 시그니처 유지:
  - `public List<CalendarEvent> findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate)`
- 구현 방식:
  - `startDate/endDate`가 둘 다 있는 경우: 기간 겹침(overlap) 조건
    - `e.startAt <= endDate 23:59:59`
    - `e.endAt >= startDate 00:00:00`
  - 둘 중 하나라도 없으면 userId 기준 전체 조회
  - 공통 정렬: `order by e.startAt asc`
- 예외 처리:
  - 결과 없으면 빈 리스트 반환(예외 미발생)

2. `src/main/java/com/example/travel_platform/calendar/CalendarService.java`
- 메서드 시그니처 유지:
  - `public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate)`
- 구현 방식:
  - Repository 호출
  - `stream().map(...)` 또는 반복문으로 `EventDTO` 매핑
  - `tripPlanId`는 `event.getTripPlan() == null ? null : event.getTripPlan().getId()`
- 입력 보정:
  - `startDate/endDate` 둘 다 존재하고 `startDate.isAfter(endDate)`면 `Exception400` 또는 정책상 빈 리스트 중 하나로 명확히 처리(프로젝트 규칙 우선)

3. `src/main/java/com/example/travel_platform/calendar/CalendarApiController.java`
- `getCalendar(...)` 목록 분기 확인:
  - `date != null` -> `getDayNode`
  - `year != null && month != null` -> `getDayNodeList`
  - 그 외 -> `getEventList(sessionUserId, startDate, endDate)`
- 목록 분기 반환 타입이 `Object`여도 현재 구조상 허용되나, 가능하면 추후 API 분리 시 타입 명확화 TODO 기록

4. 문서 기록
- `.docs/.task/.KHJ/flow/TASK01.md` 결과 섹션 갱신
- `.docs/.task/.KHJ/WORKLOG.md`에 명령 단위 기록 추가

### 8.3 메서드 구현 가이드(코드 작성 포인트)

1. Repository 쿼리 템플릿
```java
public List<CalendarEvent> findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null) {
        return em.createQuery("""
                select e
                from CalendarEvent e
                where e.user.id = :userId
                  and e.startAt <= :endDateTime
                  and e.endAt >= :startDateTime
                order by e.startAt asc
                """, CalendarEvent.class)
                .setParameter("userId", userId)
                .setParameter("startDateTime", startDate.atStartOfDay())
                .setParameter("endDateTime", endDate.plusDays(1).atStartOfDay().minusSeconds(1))
                .getResultList();
    }

    return em.createQuery("""
            select e
            from CalendarEvent e
            where e.user.id = :userId
            order by e.startAt asc
            """, CalendarEvent.class)
            .setParameter("userId", userId)
            .getResultList();
}
```

2. Service 매핑 템플릿
```java
public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
    List<CalendarEvent> events = calendarRepository.findEventListByUserId(sessionUserId, startDate, endDate);

    return events.stream().map(event -> CalendarResponse.EventDTO.builder()
            .id(event.getId())
            .tripPlanId(event.getTripPlan() == null ? null : event.getTripPlan().getId())
            .title(event.getTitle())
            .startAt(event.getStartAt())
            .endAt(event.getEndAt())
            .eventType(event.getEventType())
            .build())
            .toList();
}
```

3. 스타일 기준
- `BoardRepository`처럼 JPA Query는 Repository에서만 작성
- Service는 매핑/검증 중심으로 유지
- Controller는 세션 사용자 식별과 분기 라우팅만 담당

### 8.4 중단 조건

- 목록 조회 구현 중 Entity 필드 추가/변경 필요가 발생하는 경우
- API 응답 스펙(필드 구조) 변경이 필요한 경우
- 인증/권한 정책 변경이 필요한 경우
- 변경 규모가 파일 5개 이상 또는 diff 200줄 이상으로 확대되는 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava` 또는 `./gradlew testClasses`
- 테스트 명령: `./gradlew test --tests *Calendar*` (가능 시)
- 수동 검증 항목:
  - `GET /api/calendar` (파라미터 없음) -> 배열 JSON
  - `GET /api/calendar?startDate=2026-03-01&endDate=2026-03-31` -> 기간 겹침 데이터
  - `GET /api/calendar?startDate=2026-03-31&endDate=2026-03-01` -> 정책에 맞는 실패/빈값 처리 확인

## 10. 결과 기록(작업 후 작성)

- 변경 파일: (작업 후 기입)
- 테스트 결과: (작업 후 기입)
- 미완료 항목(TODO): (작업 후 기입)
- 리스크/주의사항: (작업 후 기입)
