# TASK09 - 챗봇 빈 메시지 입력 검증 회귀 수정

## 0. 작업 유형

- 버그 수정

## 1. 작업 목표

- `/api/chatbot/messages` 요청에서 빈 `message`가 200으로 통과되는 회귀를 수정한다.
- 기존 테스트(`ChatbotControllerTest`)의 400 응답 기대를 다시 만족시킨다.

## 2. 작업 범위

- `ChatbotRequest.AskDTO`에 입력 검증 제약 추가
- `ChatbotController`에 `@Valid` 적용
- 챗봇 테스트 및 전체 테스트 재검증
- TASK/WORKLOG/진행 문서 동기화

## 3. 작업 제외 범위

- 챗봇 SQL 생성/조회 로직 변경
- 예외 응답 포맷 변경
- 인증/권한/필터 정책 변경
- Entity/DB schema 변경

## 4. 완료 기준

- [x] 빈 `message` 요청 시 400(`CHATBOT_BAD_REQUEST`) 응답이 반환된다.
- [x] `./gradlew test --tests "*ChatbotControllerTest"`가 통과한다.
- [x] `./gradlew test` 결과를 재확인하고 문서에 반영한다.

## 5. 예상 영향 범위

- 대상 레이어: chatbot controller/request DTO/test
- 대상 기능: 챗봇 입력 검증
- 사용자 영향: 잘못된 입력 요청에 대한 오류 응답 복구

## 6. 위험도

- 등급: LOW
- 근거: 입력 검증 어노테이션 및 컨트롤러 바인딩 검증 활성화 중심

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API 스펙/DB/설정 변경 없음

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | TASK 정의/범위 확정 | AGENT_KHJ, 실패 테스트 결과 | TASK09 문서 | 문서 생성 확인 |
| 2 | 입력 검증 복구 구현 | `ChatbotRequest`, `ChatbotController` | `@NotBlank`, `@Valid` 반영 코드 | `./gradlew compileJava` |
| 3 | 테스트 및 문서 동기화 | 테스트 실행 결과 | TASK/WORKLOG/진행문서 갱신 | `./gradlew test --tests "*ChatbotControllerTest"`, `./gradlew test` |

### 8.2 중단 조건

- 범위 외 공개 API 계약 변경이 필요한 경우
- `_core` 수정이 필수로 요구되는 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`
- 테스트 명령:
  - `./gradlew test --tests "*ChatbotControllerTest"`
  - `./gradlew test --tests "*ChatbotServiceTest"`
  - `./gradlew test`
- 수동 검증 항목:
  - 빈 `message` 요청 시 400 + `CHATBOT_BAD_REQUEST` 응답 확인

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotController.java`
  - `.docs/.task/.KHJ/flow/TASK09.md`
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- 테스트 결과:
  - `./gradlew compileJava` 성공
  - `./gradlew test --tests "*ChatbotControllerTest"` 성공
  - `./gradlew test --tests "*ChatbotServiceTest"` 성공
  - `./gradlew test` 성공
- 미완료 항목(TODO):
  - SQL 실행 보안 정책(검증/화이트리스트) 적용
  - 인증/필터 경로 정책 확정 후 챗봇 연동
- 리스크/주의사항:
  - `@NotBlank`/`@Valid` 제거 시 빈 메시지 200 회귀가 재발할 수 있음
