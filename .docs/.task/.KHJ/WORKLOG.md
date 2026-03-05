# KHJ 작업 로그

## 개요
- 작성자: KHJ
- 작성일: 2026-03-03
- 목적: 팀 개발 진행 상황 기록 및 다음 작업 인계

## 오늘 완료한 작업
1. 도메인 엔티티 생성
   - `TripPlan`, `TripPlace`
   - `CommunityPost`, `CommunityReply`
   - `Booking`
   - `CalendarEvent`

2. 도메인별 계층 골격 생성 (로직은 TODO 중심)
   - `trip`, `community`, `booking`, `calendar`
   - `Repository / Service / Controller` 클래스 생성

3. 도메인별 DTO 생성
   - `Request`, `Response` 클래스 생성
   - 하위 DTO(static class)까지 역할 주석 추가

4. Lombok 어노테이션 정리
   - 엔티티의 `@Getter`, `@Setter` 조합을 `@Data`로 통일

5. 데이터 초기화 정책 정리
   - `data.sql`은 **더미 데이터 전용**으로 정리
   - `create table` 구문 제거 완료
   - FK 참조값은 더미 목적에 맞게 **하드코딩 ID 방식**으로 작성

6. 검증
   - `./gradlew test` 실행 및 통과

## 현재 상태
- 테이블 생성: 엔티티 + `ddl-auto=create`로 자동 생성
- `data.sql`: 샘플 데이터 insert만 유지
- 서비스/컨트롤러 내부 구현: 대부분 `TODO` 상태 (팀원 분담 작업 예정)

## 다음 작업
1. 세션 사용자 임시값(`1`)을 실제 세션 컨텍스트로 교체
2. Service 레이어 DTO <-> Entity 매핑 구현
3. Repository 조회/수정 쿼리 구현
4. 권한/검증 로직 구현
5. 도메인별 통합 테스트 추가

## 참고
- 현재 구조는 팀 병렬 개발을 위한 스켈레톤 우선 구성
- 실제 비즈니스 로직 구현 전, API 스펙 확정 필요

---

## 2026-03-04 추가 작업

### 1. 사용자 요청 요약
- `AGENT_KHJ.md` 기준으로 작업 진행
- 챗봇 시스템 개발 진행 단계 문서화

### 2. 진행 내용
1. 챗봇 관련 현재 코드 상태 점검
   - UI 파셜/JS/CSS 구현 범위 확인
   - 백엔드 API 및 저장 구조 유무 확인

2. 진행 단계 문서 신규 작성
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 내용: 완료/미완료 항목, 단계별 상태, 다음 작업 순서, 리스크

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서 파일 생성/내용 확인 완료
- 코드 로직 변경 없음 (문서 작업)

---

## 2026-03-04 추가 작업 (2)

### 1. 사용자 요청 요약
- `AGENT_KHJ.md` 기준으로 다음 작업 진행
- 미진행 우선순위 1건만 수행 후 `CHATBOT_PROGRESS` 업데이트

### 2. 진행 내용
1. 챗봇 API 스펙 초안 작성
   - 파일: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
   - 범위: 메시지 전송/대화 이력 조회 API, 요청/응답, 에러 코드

2. 진행 단계 문서 상태 갱신
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: API 스펙 초안 완료, 4단계 상태를 `진행중`으로 변경

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서 생성/갱신 여부 확인 완료
- 코드 로직 변경 없음 (문서 작업)

---

## 2026-03-04 추가 작업 (3)

### 1. 사용자 요청 요약
- 챗봇 동작 원리 확정
- 흐름: 자연어 질문 -> DB 필요 여부 판단 -> (필요 시) SQL 생성/조회 -> 최종 자연어 응답
- 보안 이슈는 개발 마무리 단계에서 처리

### 2. 진행 내용
1. 챗봇 API 스펙 문서 갱신
   - 파일: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
   - 반영: 분류 단계, SQL 생성 단계, 서버 조회 후 재질문 단계 명시

2. 진행 단계 문서 갱신
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: 처리 흐름 확정 완료 이력 추가, 다음 작업 우선순위 재정렬

### 3. 변경 파일
- 수정: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서 갱신 내용 확인 완료
- 코드 로직 변경 없음 (문서 작업)

---

## 2026-03-04 추가 작업 (6)

### 1. 사용자 요청 요약
- `AGENT_KHJ.md` 기준 3단계 작업 진행
- 챗봇 프론트 메시지 송수신 로직 구현

### 2. 진행 내용
1. 프론트 메시지 전송 로직 구현
   - 파일: `src/main/resources/static/js/chatbot.js`
   - 반영: 입력 이벤트/전송 이벤트 처리, API 호출(fetch), 응답 렌더링
   - 반영: 네트워크/서버 오류 시 봇 안내 메시지 출력

2. 진행 단계 문서 갱신
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: 3단계 상태를 `완료`로 변경, 다음 작업 우선순위 갱신

### 3. 변경 파일
- 수정: `src/main/resources/static/js/chatbot.js`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서/코드 변경 반영 확인 완료
- API 미구현 상태 기준 프론트 오류 처리 경로 반영

---

## 2026-03-04 추가 작업 (7)

### 1. 사용자 요청 요약
- `AGENT_KHJ.md` 기준 작업 진행
- 4단계(백엔드 챗봇 API 설계/구현) 세분화 문서 작업 수행

### 2. 진행 내용
1. 4단계 세분화 문서 신규 작성
   - 파일: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
   - 반영: 4-1 ~ 4-6 작업 단위, 완료 기준, 구현 순서, 검증 계획

2. 진행 단계 문서 갱신
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: 4단계 상태를 `세분화 문서 완료, 구현 미진행`으로 업데이트
   - 반영: 다음 작업 순서를 세분화 기준으로 조정

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서 생성/갱신 내용 확인 완료
- 코드 로직 변경 없음 (문서 작업)

---

## 2026-03-04 추가 작업 (8)

### 1. 사용자 요청 요약
- 오케스트레이터 개념 없이 구현 가능한 구조로 변경 요청
- `AGENT_KHJ.md` 기준 다음 단계 작업 진행

### 2. 진행 내용
1. 오케스트레이터 제거 및 서비스 단일 구조 전환
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일 삭제: `ChatbotOrchestrator.java`, `ChatbotOrchestratorMock.java`
   - 반영: 서비스 내부 private 메서드 기반으로 단계 분리

2. 4-3 질문 분류 단계 구현
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 반영: `needsDb/reason/queryIntent` 분류 결과 산출
   - 반영: `DIRECT_LLM/DB_QUERY` 분기 처리

3. 문서 상태 동기화
   - 파일: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: 4-2 표현을 서비스 내부 책임 분리로 변경, 4-3 완료 처리

### 3. 변경 파일
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
- 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotOrchestrator.java`
- 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotOrchestratorMock.java`
- 수정: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 코드 컴파일/테스트 확인 완료 (`./gradlew test`)
- 문서 갱신 내용 확인 완료

---

## 2026-03-04 추가 작업 (5)

### 1. 사용자 요청 요약
- 대화 내용은 저장하지 않음
- 페이지 새로고침 시 대화 내용 전체 초기화
- 프론트 화면 출력 전용으로만 사용

### 2. 진행 내용
1. 챗봇 API 스펙 재정의
   - 파일: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
   - 반영: 대화 이력 API 제거, 단일 질문 처리 API 중심으로 수정
   - 반영: `conversationId/messageId` 기반 저장 모델 제거

2. 진행 단계 문서 갱신
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: 대화 저장 단계를 `제외`로 변경
   - 반영: 비저장 원칙 확정 이력 추가

### 3. 변경 파일
- 수정: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서 갱신 내용 확인 완료
- 코드 로직 변경 없음 (문서 작업)

---

## 2026-03-04 추가 작업 (4)

### 1. 사용자 요청 요약
- 인증은 현재 단계에서 적용하지 않음
- 필터 경로는 `/boards/*`, `/replies/*` 외 다른 경로를 사용할 예정이나 미확정 상태

### 2. 진행 내용
1. 챗봇 API 스펙 문서 수정
   - 파일: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
   - 반영: 인증/필터 적용 보류 정책 명시, 401/403 코드는 예약 코드로 분리

2. 진행 단계 문서 수정
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영: 권한/세션 단계 상태를 `보류`로 조정, 인증/필터 미확정 리스크 반영

### 3. 변경 파일
- 수정: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- 문서 갱신 내용 확인 완료
- 코드 로직 변경 없음 (문서 작업)

---

## 2026-03-05 추가 작업 (1)

### 1. 사용자 요청 요약
- `AGENT_KHJ.md` 기준으로 다음 작업 진행
- 챗봇 4-4/4-5/4-6 구현 및 문서 동기화

### 2. 진행 내용
1. TASK 문서 우선 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK01.md`
   - 반영: 작업 범위/제외 범위/위험도/Workflow/검증 계획 수립

2. 챗봇 4-4 SQL 생성 및 DB 조회 구현
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 반영: `queryIntent`별 SQL 계획 생성, `JdbcTemplate` 조회, `rowCount` 생성

3. 챗봇 4-5 최종 답변 생성 구현
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
   - 반영: `DIRECT_LLM/DB_QUERY` 분기 응답, `meta` 확장(`querySummary/generatedSql/rowCount`)

4. 챗봇 4-6 예외/응답 형식 정렬
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotException.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotErrorResponse.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
   - 반영: 챗봇 API 전용 JSON 오류 응답(`CHATBOT_BAD_REQUEST`, `CHATBOT_INTERNAL_ERROR`)

5. 단위 테스트 추가
   - 파일: `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
   - 반영: DIRECT_LLM/DB_QUERY/DB 오류 경로 검증

6. 진행 문서 동기화
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 파일: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
   - 파일: `.docs/.task/.KHJ/flow/TASK01.md`
   - 반영: 4-4~4-6 완료 상태, 검증 결과, 다음 작업 업데이트

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK01.md`
- 추가: `src/main/java/com/example/travel_platform/chatbot/ChatbotException.java`
- 추가: `src/main/java/com/example/travel_platform/chatbot/ChatbotErrorResponse.java`
- 추가: `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
- 추가: `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공
- `./gradlew test --tests "*ChatbotServiceTest"` 성공
- `./gradlew test` 실패
  - 원인: 기존 `UserRepositoryTest`의 H2 SQL 문법 이슈(본 작업 범위 외)

---

## 2026-03-05 추가 작업 (2)

### 1. 사용자 요청 요약
- 예외 클래스 파일 위치 정리 요청
- `ChatbotException`을 예외 공통 경로(`_core/handler/ex`)로 이동

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK02.md`
   - 반영: 목표/범위/검증 계획 수립

2. 예외 클래스 위치 정리
   - 추가: `src/main/java/com/example/travel_platform/_core/handler/ex/ChatbotException.java`
   - 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotException.java`
   - 수정: `ChatbotService`, `ChatbotExceptionHandler` import 경로 정리

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK02.md`
- 추가: `src/main/java/com/example/travel_platform/_core/handler/ex/ChatbotException.java`
- 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotException.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공

---

## 2026-03-05 추가 작업 (3)

### 1. 사용자 요청 요약
- 챗봇 도메인 내 기능별 위치 정리 요청
- `ChatbotException`을 `chatbot/exception` 하위로 분리

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK03.md`
   - 반영: 범위/완료 기준/검증 계획 수립

2. 예외 클래스 위치 조정
   - 추가: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotException.java`
   - 삭제: `src/main/java/com/example/travel_platform/_core/handler/ex/ChatbotException.java`
   - 수정: `ChatbotService`, `ChatbotExceptionHandler` import 경로 조정

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK03.md`
- 추가: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotException.java`
- 삭제: `src/main/java/com/example/travel_platform/_core/handler/ex/ChatbotException.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공

---

## 2026-03-05 추가 작업 (4)

### 1. 사용자 요청 요약
- `ChatbotErrorResponse`, `ChatbotExceptionHandler`를 `chatbot/exception` 폴더로 이동 요청

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK04.md`
   - 반영: 작업 범위/완료 기준/검증 계획 수립

2. 파일 위치 정리
   - 추가: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotErrorResponse.java`
   - 추가: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotExceptionHandler.java`
   - 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotErrorResponse.java`
   - 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
   - 반영: 핸들러에서 `ChatbotController` import 유지, 예외 응답/핸들러 동작은 동일 유지

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK04.md`
- 추가: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotErrorResponse.java`
- 추가: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotExceptionHandler.java`
- 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotErrorResponse.java`
- 삭제: `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공

---

## 2026-03-05 추가 작업 (5)

### 1. 사용자 요청 요약
- `AGENT_KHJ` 기준으로 다음 작업 진행 요청
- 챗봇 미완료 우선순위 항목(컨트롤러/예외 처리 통합 테스트) 수행

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK05.md`
   - 반영: 테스트 보강 범위/완료 기준/검증 계획 수립

2. 챗봇 컨트롤러 통합 테스트 추가
   - 파일: `src/test/java/com/example/travel_platform/chatbot/ChatbotControllerTest.java`
   - 반영:
     - 정상 요청 200 응답 검증
     - `message` 검증 실패 400 JSON 응답 검증
     - 서비스 내부 예외 500 JSON 응답 검증
     - JSON 파싱 오류 400 JSON 응답 검증

3. 진행 문서 동기화
   - 파일: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
   - 반영:
     - 통합 테스트 완료 항목 추가
     - 예외 클래스 최신 경로(`chatbot/exception`) 반영
     - 다음 작업 우선순위 갱신

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK05.md`
- 추가: `src/test/java/com/example/travel_platform/chatbot/ChatbotControllerTest.java`
- 수정: `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew test --tests "*ChatbotControllerTest"` 성공
- `./gradlew test --tests "*ChatbotServiceTest"` 성공
- `./gradlew compileJava` 성공

---

## 2026-03-05 추가 작업 (6)

### 1. 사용자 요청 요약
- `AGENT_KHJ` 기준으로 전체 구조/코드컨벤션 리팩토링 우선 진행 요청

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK06.md`
   - 반영: 컨트롤러 구조/경로 리팩토링 범위와 검증 계획 정의

2. REST 컨트롤러 네이밍/경로 정리
   - `TripController` -> `TripApiController` (`/api/trip-plans`)
   - `CommunityController` -> `CommunityApiController` (`/api/community/posts`)
   - `CalendarController` -> `CalendarApiController` (`/api/calendar/events`)

3. Booking 도메인 SSR/REST 분리
   - `BookingController`는 SSR(뷰 반환) 전용으로 정리
   - `BookingApiController` 신규 생성하여 JSON API(`/api/bookings`) 처리

4. 프론트 API 호출 경로 동기화
   - 파일: `src/main/resources/static/js/map-detail.js`
   - 반영:
     - `/bookings/place-image` -> `/api/bookings/place-image`
     - `/bookings/map-pois/merge` -> `/api/bookings/map-pois/merge`

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK06.md`
- 추가: `src/main/java/com/example/travel_platform/trip/TripApiController.java`
- 삭제: `src/main/java/com/example/travel_platform/trip/TripController.java`
- 추가: `src/main/java/com/example/travel_platform/community/CommunityApiController.java`
- 삭제: `src/main/java/com/example/travel_platform/community/CommunityController.java`
- 추가: `src/main/java/com/example/travel_platform/calendar/CalendarApiController.java`
- 삭제: `src/main/java/com/example/travel_platform/calendar/CalendarController.java`
- 추가: `src/main/java/com/example/travel_platform/booking/BookingApiController.java`
- 수정: `src/main/java/com/example/travel_platform/booking/BookingController.java`
- 수정: `src/main/resources/static/js/map-detail.js`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공
- `./gradlew test --tests "*ChatbotControllerTest"` 성공
- `./gradlew test --tests "*ChatbotServiceTest"` 성공

---

## 2026-03-05 추가 작업 (7)

### 1. 사용자 요청 요약
- 전체 구조/코드컨벤션 리팩토링 우선 진행
- 서비스 계층 컨벤션 정리(트랜잭션/메서드 네이밍) 추가 진행

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK07.md`
   - 반영: 서비스 컨벤션 정리 범위/완료 기준/검증 계획 수립

2. 서비스 트랜잭션 선언 정리
   - 대상: `TripService`, `CommunityService`, `CalendarService`, `BookingService`, `UserService`
   - 반영:
     - `jakarta.transaction.Transactional` -> `org.springframework.transaction.annotation.Transactional`
     - 클래스 레벨 `@Transactional(readOnly = true)` 적용
     - 쓰기 메서드의 메서드 레벨 `@Transactional` 유지

3. UserService 메서드 네이밍 정리
   - `회원가입` -> `join`
   - `로그인` -> `login`
   - `UserController` 호출부 동기화 반영

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK07.md`
- 수정: `src/main/java/com/example/travel_platform/trip/TripService.java`
- 수정: `src/main/java/com/example/travel_platform/community/CommunityService.java`
- 수정: `src/main/java/com/example/travel_platform/calendar/CalendarService.java`
- 수정: `src/main/java/com/example/travel_platform/booking/BookingService.java`
- 수정: `src/main/java/com/example/travel_platform/user/UserService.java`
- 수정: `src/main/java/com/example/travel_platform/user/UserController.java`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공

---

## 2026-03-05 추가 작업 (8)

### 1. 사용자 요청 요약
- `community` 도메인을 `board`로 변경
- 게시글/댓글 혼합 구조를 분리해 댓글 도메인을 하위 폴더로 배치
- `board`는 뷰 응답용 `@Controller` 중심 구성(`RestController` 미사용)
- 캘린더 페이지 응답용 컨트롤러 추가

### 2. 진행 내용
1. TASK 문서 작성
   - 파일: `.docs/.task/.KHJ/flow/TASK08.md`
   - 반영: 도메인 전환/구조 분리/컨트롤러 전환 범위 정의

2. community -> board 도메인 전환
   - 추가:
     - `board/Board.java`
     - `board/BoardRequest.java`
     - `board/BoardResponse.java`
     - `board/BoardRepository.java`
     - `board/BoardService.java`
     - `board/BoardController.java`
   - 추가:
     - `board/reply/BoardReply.java` (댓글 도메인 하위 분리)
   - 삭제:
     - `community` 패키지 전체 파일(`Community*`)

3. 컨트롤러 정책 반영
   - `BoardController`를 `@Controller` 기반 뷰 반환 전용으로 구성
   - `CalendarController` 추가로 `pages/calendar` 뷰 라우트 보강

4. 참조 동기화
   - `ChatbotService`의 게시글 조회 테이블 참조를 `board_tb`로 변경
   - 게시판 관련 분류 키워드에 `board/게시판` 보강

### 3. 변경 파일
- 추가: `.docs/.task/.KHJ/flow/TASK08.md`
- 추가: `src/main/java/com/example/travel_platform/board/Board.java`
- 추가: `src/main/java/com/example/travel_platform/board/reply/BoardReply.java`
- 추가: `src/main/java/com/example/travel_platform/board/BoardRequest.java`
- 추가: `src/main/java/com/example/travel_platform/board/BoardResponse.java`
- 추가: `src/main/java/com/example/travel_platform/board/BoardRepository.java`
- 추가: `src/main/java/com/example/travel_platform/board/BoardService.java`
- 추가: `src/main/java/com/example/travel_platform/board/BoardController.java`
- 추가: `src/main/java/com/example/travel_platform/calendar/CalendarController.java`
- 삭제: `src/main/java/com/example/travel_platform/community/*`
- 수정: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
- 수정: `.docs/.task/.KHJ/WORKLOG.md`

### 4. 검증
- `./gradlew compileJava` 성공
- `./gradlew test --tests "*ChatbotServiceTest"` 성공
- `./gradlew test --tests "*ChatbotControllerTest"` 성공

