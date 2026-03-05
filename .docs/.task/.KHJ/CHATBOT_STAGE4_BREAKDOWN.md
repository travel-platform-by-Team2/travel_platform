# 챗봇 4단계 세분화 문서

## 0. 문서 정보
- 작성일: 2026-03-04
- 기준: `.docs/.task/.KHJ/AGENT_KHJ.md`, `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`, `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
- 목적: 4단계(백엔드 챗봇 API 설계/구현) 작업을 구현 단위로 세분화

## 1. 4단계 목표
- `POST /api/chatbot/messages`를 백엔드에서 실제 처리 가능 상태로 만든다.
- 비저장 원칙(서버/DB 대화 로그 미저장)을 유지한다.
- 인증/필터는 현재 단계에서 미적용으로 유지한다.

## 2. 세분화 작업 목록

### 4-1. API 엔드포인트 뼈대 구성
- 상태: 완료 (2026-03-04)
- 대상: `src/main/java/com/example/travel_platform/chatbot`
- 생성:
  - `ChatbotController`
  - `ChatbotService`
  - `ChatbotRequest`
  - `ChatbotResponse`
- 완료 기준:
  - `POST /api/chatbot/messages` 라우팅 가능
  - 요청 DTO 검증(`message` 필수) 동작

### 4-2. 서비스 내부 책임 분리
- 상태: 완료 (2026-03-04)
- 목적: 컨트롤러/서비스 역할 분리를 유지하면서 서비스 내부 단계 분리 기반으로 구성
- 반영:
  - `ChatbotService` 내 단계별 private 메서드 분리 기반으로 구조화
- 완료 기준:
  - 서비스가 요청 입력부터 응답 반환까지 단일 진입점(`ask`)으로 처리
  - 오케스트레이터 개념 없이도 다음 단계 확장이 가능한 구조 확보

### 4-3. 질문 분류 단계 구현
- 상태: 완료 (2026-03-04)
- 입력: 사용자 질문
- 출력:
  - `needsDb` (Boolean)
  - `reason` (String)
  - `queryIntent` (String, 선택)
- 임시 정책:
  - 키워드 기반 분류(예: 예약/일정/게시글/여행계획)
- 완료 기준:
  - `DIRECT_LLM`/`DB_QUERY` 분기 값 산출

### 4-4. SQL 생성 및 DB 조회 단계 구현
- 상태: 완료 (2026-03-05)
- 전제: 보안 검증은 후순위
- 처리:
  - `needsDb=true`인 경우 `queryIntent` 기준 SQL 계획 생성
  - `JdbcTemplate` 기반 DB 조회 실행
  - 결과를 JSON 직렬화 가능한 형태(`List<Map<String, Object>>`)로 구성
- 반영:
  - `ChatbotService`에 SQL 생성/조회 단계 추가
  - `TripPlan` 조회는 `context.tripPlanId` 존재 시 파라미터 바인딩 적용
- 완료 기준:
  - 조회 성공 시 rowCount/meta 생성
  - 조회 실패 시 챗봇 내부 오류 응답 처리

### 4-5. 최종 답변 생성 단계 구현
- 상태: 완료 (2026-03-05)
- 입력:
  - 원 질문
  - 조회 결과(DB 필요 시)
  - 분기 정보(`needsDb`, `querySummary`)
- 출력:
  - `answer`
  - `processingType`
  - `meta`
- 반영:
  - `DIRECT_LLM`/`DB_QUERY` 분기별 답변 생성 로직 구현
  - `meta`에 `querySummary`, `generatedSql`, `rowCount` 확장
- 완료 기준:
  - 프론트에서 즉시 렌더링 가능한 응답 스키마 반환

### 4-6. 예외/응답 형식 정렬
- 상태: 완료 (2026-03-05)
- 현황:
  - 현재 전역 예외는 HTML script 응답 중심
- 조치:
  - 챗봇 API 경로에서는 JSON 응답을 우선 보장
  - 최소 오류 코드: `CHATBOT_BAD_REQUEST`, `CHATBOT_INTERNAL_ERROR`
- 반영:
  - `ChatbotException`, `ChatbotErrorResponse`, `ChatbotExceptionHandler` 추가
  - 입력값 검증/JSON 파싱 오류/내부 오류에 대한 챗봇 전용 JSON 오류 응답 적용
- 완료 기준:
  - 프론트 fetch 기준 파싱 가능한 오류 응답 확보

## 3. 구현 순서 제안
1. `4-1` 엔드포인트 뼈대
2. `4-2` 서비스 내부 책임 분리
3. `4-3` 질문 분류
4. `4-4` SQL/DB 조회
5. `4-5` 최종 답변
6. `4-6` 예외 응답 정렬

## 4. 검증 계획
1. 빌드/테스트
   - `./gradlew compileJava` (성공)
   - `./gradlew test --tests "*ChatbotServiceTest"` (성공)
   - `./gradlew test` (실패: 기존 `UserRepositoryTest`의 H2 SQL 문법 이슈, 본 작업 범위 외)
2. 수동 API 확인
   - `POST /api/chatbot/messages`에 질문 전송
   - `processingType`, `answer`, `meta` 응답 확인
3. 프론트 연동 확인
   - 챗봇 창 입력 후 봇 답변 렌더링 확인
   - API 실패 시 안내 메시지 노출 확인

## 5. 제외/보류 항목
1. 인증/필터 적용
2. SQL 보안(검증/화이트리스트)
3. 대화 로그 저장/이력 조회
4. 운영 환경 모니터링/레이트 리밋

