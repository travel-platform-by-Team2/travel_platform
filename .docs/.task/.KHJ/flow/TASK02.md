# TASK02 - ChatbotException 위치 정리

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- 챗봇 전용 예외 클래스(`ChatbotException`)를 예외 공통 위치 규칙에 맞춰 `_core/handler/ex`로 이동한다.

## 2. 작업 범위

- `ChatbotException` 파일 경로/패키지 이동
- 참조 import 정리(`ChatbotService`, `ChatbotExceptionHandler`)
- 컴파일 검증

## 3. 작업 제외 범위

- 예외 처리 로직 동작 변경
- 응답 DTO/핸들러 동작 변경
- 챗봇 기능/스펙 변경

## 4. 완료 기준

- [x] `ChatbotException`이 `_core/handler/ex`에 위치한다.
- [x] 챗봇 도메인에서 새 패키지를 참조해 정상 컴파일된다.

## 5. 예상 영향 범위

- 대상 레이어: 예외 클래스 패키지 구조
- 대상 기능: 챗봇 예외 처리 경로
- 사용자 영향: 없음(내부 구조 정리)

## 6. 위험도

- 등급: LOW
- 근거: 클래스 이동 + import 정리만 수행

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API/DB/설정 변경 없음

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 예외 클래스 이동 | 기존 `chatbot/ChatbotException.java` | `_core/handler/ex/ChatbotException.java` | 파일 존재 확인 |
| 2 | 참조 정리 | `ChatbotService`, `ChatbotExceptionHandler` | import 정리된 코드 | `./gradlew compileJava` |

### 8.2 중단 조건

- `_core` 내 다른 광범위 수정이 필요한 경우
- 범위 외 스펙 변경 요구 발생 시

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/_core/handler/ex/ChatbotException.java` (추가)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotException.java` (삭제)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java` (import 수정)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java` (import 수정)
  - `.docs/.task/.KHJ/flow/TASK02.md` (결과 기록)
  - `.docs/.task/.KHJ/WORKLOG.md` (이력 기록)
- 테스트 결과:
  - `./gradlew compileJava` 성공
- 미완료 항목(TODO):
  - 없음
- 리스크/주의사항:
  - 동작 변경 없이 위치/참조만 정리
