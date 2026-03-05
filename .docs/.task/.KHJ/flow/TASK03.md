# TASK03 - 챗봇 예외 클래스 도메인 하위 분리

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- `ChatbotException`을 챗봇 도메인 하위 `exception` 패키지로 이동해 기능별 위치를 명확히 한다.

## 2. 작업 범위

- `ChatbotException` 파일 이동
- `ChatbotService`, `ChatbotExceptionHandler` import 정리
- 컴파일 검증

## 3. 작업 제외 범위

- 예외 응답 스펙 변경
- 핸들러 동작 로직 변경
- 다른 도메인 파일 구조 변경

## 4. 완료 기준

- [x] `ChatbotException` 경로가 `chatbot/exception`으로 이동한다.
- [x] 참조 코드가 새 패키지를 사용해 컴파일된다.

## 5. 예상 영향 범위

- 대상 레이어: chatbot 예외 구조
- 대상 기능: 챗봇 예외 발생/처리
- 사용자 영향: 없음

## 6. 위험도

- 등급: LOW
- 근거: 파일 이동 + import 수정 중심

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: 공개 API/DB/설정 변경 없음

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 예외 클래스 이동 | 기존 `ChatbotException` 파일 | `chatbot/exception/ChatbotException.java` | 파일/패키지 확인 |
| 2 | 참조 코드 정리 | Service/Handler 코드 | import 수정본 | `./gradlew compileJava` |

### 8.2 중단 조건

- 범위 외 대규모 구조 변경 요구 발생 시
- 예외 처리 정책 자체 변경 요구 발생 시

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotException.java` (추가)
  - `src/main/java/com/example/travel_platform/_core/handler/ex/ChatbotException.java` (삭제)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java` (import 수정)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotExceptionHandler.java` (import 수정)
  - `.docs/.task/.KHJ/flow/TASK03.md` (결과 기록)
  - `.docs/.task/.KHJ/WORKLOG.md` (이력 기록)
- 테스트 결과:
  - `./gradlew compileJava` 성공
- 미완료 항목(TODO):
  - 없음
- 리스크/주의사항:
  - 동작 변경 없이 파일 위치/참조만 정리
