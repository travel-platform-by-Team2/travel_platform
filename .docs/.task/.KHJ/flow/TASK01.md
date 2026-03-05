# TASK01 - 챗봇 4-4~4-6 백엔드 처리 구현

## 0. 작업 유형

- 기능 추가

## 1. 작업 목표

- 챗봇 API(`POST /api/chatbot/messages`)에 DB 조회 기반 응답 흐름을 구현한다.
- 챗봇 API 오류를 JSON 형식으로 일관되게 반환한다.

## 2. 작업 범위

- `ChatbotService`에 SQL 생성/DB 조회/최종 답변 생성 단계 구현
- 챗봇 응답 DTO 메타 확장(`querySummary`, `generatedSql`, `rowCount`)
- 챗봇 전용 예외 응답 DTO/핸들러 추가
- 챗봇 진행 문서 및 작업 로그 업데이트

## 3. 작업 제외 범위

- 인증/인가 로직 추가
- SQL 보안(화이트리스트/검증) 강화
- 대화 저장 기능 추가
- `_core` 폴더 코드 수정

## 4. 완료 기준

- [x] `needsDb=true` 질문에서 SQL 생성 및 DB 조회가 수행된다.
- [x] 응답 `meta`에 `querySummary/generatedSql/rowCount`가 포함된다(해당 시).
- [x] 챗봇 API 오류가 JSON(`code/message/status/timestamp`)으로 반환된다.

## 5. 예상 영향 범위

- 대상 레이어: chatbot(Service/DTO/ControllerAdvice), 문서
- 대상 기능: 챗봇 메시지 처리 API
- 사용자 영향: 챗봇 답변 품질 및 오류 응답 형식 개선

## 6. 위험도

- 등급: MEDIUM
- 근거: 런타임 SQL 실행 추가 및 예외 응답 방식 분기 도입

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: URL/요청 바디 변경 없이 기존 스펙 초안 범위 내 구현

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 서비스 로직 확장 설계 | `CHATBOT_STAGE4_BREAKDOWN`, 기존 `ChatbotService` | queryIntent별 SQL/응답 생성 설계 | 코드 리뷰 관점 셀프 점검 |
| 2 | 4-4/4-5 구현 | 사용자 질문, 분류 결과 | SQL 계획, DB 조회 결과, 최종 답변 | `./gradlew compileJava` |
| 3 | 4-6 예외 응답 구현 | 검증/런타임 예외 케이스 | 챗봇 전용 JSON 오류 응답 | 수동 API 확인 + 컴파일 |
| 4 | 문서 동기화 및 결과 기록 | 변경 파일 목록/검증 결과 | 진행 문서 및 WORKLOG 업데이트 | 문서 내용/경로 점검 |

### 8.2 중단 조건

- `_core` 수정이 불가피해지는 경우
- 공개 API 경로/필수 응답 필드 변경이 필요한 경우
- 범위 외 대규모 리팩토링 요구가 발생하는 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`
- 테스트 명령: `./gradlew test`
- 수동 검증 항목:
  - `POST /api/chatbot/messages` 일반 질문 응답(`DIRECT_LLM`)
  - `POST /api/chatbot/messages` DB 조회 질문 응답(`DB_QUERY`)
  - 빈 message 요청 시 JSON 에러 응답 확인

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotException.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotErrorResponse.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java`
  - `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
  - `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
  - `.docs/.task/.KHJ/flow/TASK01.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- 테스트 결과:
  - `./gradlew compileJava` 성공
  - `./gradlew test --tests "*ChatbotServiceTest"` 성공
  - `./gradlew test` 실패(기존 `UserRepositoryTest` H2 SQL 문법 이슈)
- 미완료 항목(TODO):
  - 챗봇 컨트롤러/예외 처리 통합 테스트 추가
  - SQL 보안 정책 적용(화이트리스트/검증)
- 리스크/주의사항:
  - SQL 실행 보안은 현재 후순위 정책으로 미적용 상태
  - 전체 테스트 실패 원인은 본 TASK 범위 외 기존 테스트 이슈
