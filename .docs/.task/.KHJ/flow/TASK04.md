# TASK04 - 챗봇 예외 핸들러/응답 클래스 위치 정리

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- `ChatbotErrorResponse`, `ChatbotExceptionHandler`를 `chatbot/exception` 패키지로 이동해 챗봇 예외 관련 클래스를 한곳에 모은다.

## 2. 작업 범위

- 파일 이동(2개)
- 패키지/참조(import) 정리
- 컴파일 검증

## 3. 작업 제외 범위

- 예외 응답 포맷 변경
- 챗봇 API 스펙 변경
- 도메인 로직 변경

## 4. 완료 기준

- [x] `ChatbotErrorResponse`가 `chatbot/exception`에 위치한다.
- [x] `ChatbotExceptionHandler`가 `chatbot/exception`에 위치한다.
- [x] 변경 후 `./gradlew compileJava` 성공

## 5. 예상 영향 범위

- 대상 레이어: chatbot 예외 처리 구조
- 대상 기능: 챗봇 API 예외 처리
- 사용자 영향: 없음

## 6. 위험도

- 등급: LOW
- 근거: 위치 이동 + import 정리만 수행

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API/DB/설정 변경 없음

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 파일 이동 | 기존 2개 클래스 | `chatbot/exception` 하위 클래스 | 파일/패키지 확인 |
| 2 | 참조 정리 | 핸들러/서비스/컨트롤러 참조 | import 정리본 | `./gradlew compileJava` |

### 8.2 중단 조건

- 범위 외 대규모 구조 변경 요구 발생 시
- 예외 정책 자체 변경 요구 발생 시

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotErrorResponse.java` (추가)
  - `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotExceptionHandler.java` (추가)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotErrorResponse.java` (삭제)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java` (삭제)
  - `.docs/.task/.KHJ/flow/TASK04.md` (결과 기록)
  - `.docs/.task/.KHJ/WORKLOG.md` (이력 기록)
- 테스트 결과:
  - `./gradlew compileJava` 성공
- 미완료 항목(TODO):
  - 없음
- 리스크/주의사항:
  - 클래스 위치만 변경, 예외 동작/응답 스펙은 유지
