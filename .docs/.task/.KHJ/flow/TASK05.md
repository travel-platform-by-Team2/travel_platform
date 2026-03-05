# TASK05 - 챗봇 컨트롤러/예외 처리 통합 테스트 추가

## 0. 작업 유형

- 테스트 보강

## 1. 작업 목표

- 챗봇 API 컨트롤러와 예외 핸들러가 실제 HTTP 요청 기준으로 의도한 JSON 응답을 반환하는지 검증한다.

## 2. 작업 범위

- `@WebMvcTest` + `MockMvc` 기반 통합 테스트 추가
- 검증 대상:
  - 정상 요청 성공 응답(200)
  - 입력 검증 실패 응답(400, `CHATBOT_BAD_REQUEST`)
  - 서비스 내부 예외 응답(500, `CHATBOT_INTERNAL_ERROR`)
- 진행 문서/작업 로그 업데이트

## 3. 작업 제외 범위

- 챗봇 서비스 로직 변경
- SQL 보안 정책 적용
- 전체 테스트 실패(`UserRepositoryTest`) 수정

## 4. 완료 기준

- [x] 챗봇 컨트롤러 통합 테스트가 추가된다.
- [x] 예외 핸들러 JSON 응답 포맷 검증 테스트가 포함된다.
- [x] `./gradlew test --tests "*ChatbotControllerTest"`가 통과한다.

## 5. 예상 영향 범위

- 대상 레이어: chatbot API 테스트
- 대상 기능: `/api/chatbot/messages` 요청/응답 검증
- 사용자 영향: 없음(내부 품질 개선)

## 6. 위험도

- 등급: LOW
- 근거: 운영 코드 변경 없이 테스트 코드 추가 중심

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API/DB/설정 변경 없음

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 테스트 시나리오 정의 | 챗봇 API/예외 스펙 | 케이스 목록(성공/400/500) | 케이스 누락 점검 |
| 2 | 통합 테스트 구현 | `ChatbotController`, `ChatbotExceptionHandler` | `ChatbotControllerTest` | `./gradlew test --tests "*ChatbotControllerTest"` |
| 3 | 문서 동기화 | 테스트 결과/변경 파일 | TASK/진행문서/WORKLOG 갱신 | 문서 내용 점검 |

### 8.2 중단 조건

- 컨트롤러 시그니처/공개 스펙 변경이 필요한 경우
- 범위 외 구조 리팩토링이 필요한 경우

## 9. 검증 계획

- 테스트 명령:
  - `./gradlew test --tests "*ChatbotControllerTest"`
  - `./gradlew test --tests "*ChatbotServiceTest"`
  - `./gradlew compileJava`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/test/java/com/example/travel_platform/chatbot/ChatbotControllerTest.java` (추가)
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md` (수정)
  - `.docs/.task/.KHJ/flow/TASK05.md` (결과 기록)
  - `.docs/.task/.KHJ/WORKLOG.md` (이력 기록)
- 테스트 결과:
  - `./gradlew test --tests "*ChatbotControllerTest"` 성공
  - `./gradlew test --tests "*ChatbotServiceTest"` 성공
  - `./gradlew compileJava` 성공
- 미완료 항목(TODO):
  - SQL 보안 정책(검증/화이트리스트) 적용
  - 전체 테스트(`./gradlew test`) 안정화
- 리스크/주의사항:
  - 기존 `UserRepositoryTest` H2 SQL 문법 이슈는 본 TASK 범위 외
