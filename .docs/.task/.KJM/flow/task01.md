# TASK01 - 캘린더 일정 목록 조회 구현 상세 Flow

## 0. 작업 유형

- 기능 추가

## 1. 작업 목표

- `GET /api/calendar` 요청에서 일정 목록 조회가 실제 DB 데이터로 동작하도록 구현한다.
- 계층 구조(`CalendarApiController -> CalendarService -> CalendarRepository`)를 유지한다.

## 2. 작업 범위

- `CalendarRepository.findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate)` 구현
- `CalendarService.getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate)` 구현
- `CalendarApiController.getCalendar(...)` 목록 분기 동작 점검
- 최소 검증(컴파일/테스트) 및 결과 기록

## 3. 작업 제외 범위

- Entity/DB schema 변경
- 인증/권한 정책 변경
- 월별/일별 노드(`getDayNodeList`, `getDayNode`) 구현
- 일정 생성/수정/삭제 로직 구현

## 4. 완료 기준

- [ ] `GET /api/calendar`가 `List<CalendarResponse.EventDTO>` 형식의 일정 목록을 반환한다.
- [ ] `startDate`, `endDate`가 둘 다 있으면 기간 겹침 조건으로 조회된다.
- [ ] 계층 규칙(Controller -> Service -> Repository)을 위반하지 않는다.
- [ ] 검증 결과와 미완료 항목이 문서에 기록된다.

## 5. 예상 영향 범위

- 대상 레이어: calendar controller/service/repository
- 대상 기능: 일정 목록 조회 API
- 사용자 영향: 캘린더 화면 일정 데이터 노출

## 6. 위험도

- 등급: MEDIUM
- 근거: 조회 조건/DTO 매핑 오류 시 빈 목록, 누락, 정렬 문제 가능성 있음

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API 스펙/DB schema 변경 없이 기존 구조 내 구현

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 코드 계약 확인 | Controller/Service/Repository 현재 시그니처 | 구현 대상 메서드 목록 | 호출 시그니처 불일치 여부 확인 |
| 2 | Repository 구현 | userId, startDate, endDate, Entity 필드 | JPQL 조회 코드 | 기간 조건/정렬 조건 확인 |
| 3 | Service 구현 | Repository 반환 리스트 | EventDTO 매핑 코드 | 필드 누락 여부 확인 |
| 4 | Controller 점검 | getCalendar 분기 로직 | 호출 경로 점검 결과 | date/year+month/list 분기 우선순위 확인 |
| 5 | 검증 및 기록 | 빌드/테스트 명령 결과 | TASK 결과 업데이트 | 명령/결과/실패 사유 기록 |

### 8.2 파일별 구현 순서 (따라하기용)

1. `src/main/java/com/example/travel_platform/calendar/CalendarRepository.java`
- 작업 메서드: `findEventListByUserId(Integer userId, LocalDate startDate, LocalDate endDate)`
- 구현 순서:
  1. `if (startDate != null && endDate != null)` 분기 작성
  2. 기간 겹침 조건 JPQL 작성
  3. 기간 미지정 분기 JPQL 작성
  4. 두 분기 모두 `order by e.startAt asc` 적용
- 기간 겹침 조건:
  - `e.startAt <= endDate 23:59:59`
  - `e.endAt >= startDate 00:00:00`
- 체크포인트:
  - 결과가 없을 때 빈 리스트 반환
  - `userId` 조건이 빠지지 않았는지 확인

2. `src/main/java/com/example/travel_platform/calendar/CalendarService.java`
- 작업 메서드: `getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate)`
- 구현 순서:
  1. 날짜 역전 검증(`startDate > endDate`) 처리
  2. Repository 메서드 호출
  3. `CalendarResponse.EventDTO`로 매핑
  4. 리스트 반환
- 필수 매핑 필드:
  - `id`, `tripPlanId`, `title`, `startAt`, `endAt`, `eventType`
- 체크포인트:
  - `tripPlan` null-safe 처리
  - stream 매핑 후 리스트 반환 타입 확인

3. `src/main/java/com/example/travel_platform/calendar/CalendarApiController.java`
- 점검 메서드: `getCalendar(...)`
- 점검 순서:
  1. `date != null` 분기 유지 확인
  2. `year != null && month != null` 분기 유지 확인
  3. 기본 분기가 `getEventList(sessionUserId, startDate, endDate)` 호출인지 확인
- 체크포인트:
  - 세션 사용자 ID는 `requireSessionUserId()`로만 가져오도록 유지

4. 문서 기록
- `.docs/.task/.KJM/flow/TASK01.md`
  - 완료 기준 체크박스 갱신
  - 결과 기록 섹션 작성
- `.docs/.task/.KJM/WORKLOG.md`
  - 명령 번호 증가 후 변경 파일/검증 결과 기록

### 8.3 구현 템플릿 (복붙 기준)

1. Repository 템플릿
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

2. Service 템플릿
```java
public List<CalendarResponse.EventDTO> getEventList(Integer sessionUserId, LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
        throw new Exception400("startDate는 endDate보다 늦을 수 없습니다.");
    }

    List<CalendarEvent> events = calendarRepository.findEventListByUserId(sessionUserId, startDate, endDate);

    return events.stream()
            .map(event -> CalendarResponse.EventDTO.builder()
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

### 8.4 중단 조건

- Entity/DB schema 변경이 필요해지는 경우
- 응답 스펙 변경이 필요한 경우
- 인증/권한 로직 변경이 필요한 경우
- 변경 파일 5개 이상 또는 diff 200줄 이상으로 확대되는 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`
- 테스트 명령: `./gradlew test --tests *Calendar*` (가능 시)
- 수동 검증:
  - `GET /api/calendar`
  - `GET /api/calendar?startDate=2026-03-01&endDate=2026-03-31`
  - `GET /api/calendar?startDate=2026-03-31&endDate=2026-03-01`

## 10. 결과 기록(작업 후 작성)

- 변경 파일: (작업 후 기입)
- 테스트 결과: (작업 후 기입)
- 미완료 항목(TODO): (작업 후 기입)
- 리스크/주의사항: (작업 후 기입)
