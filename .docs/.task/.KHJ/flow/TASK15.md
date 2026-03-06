# TASK15 - 챗봇 프롬프트 한글화 및 도메인 주석 보강

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- OpenAI 연동 프롬프트를 한국어로 정리해 팀원이 바로 이해할 수 있도록 개선한다.
- `chatbot` 도메인 코드에 상세 주석을 추가해 유지보수 난이도를 낮춘다.

## 2. 작업 범위

- `src/main/java/com/example/travel_platform/chatbot/**` 주석 보강
- `OpenAiChatbotLlmClient` 시스템 프롬프트 한국어화
- 컴파일/테스트 검증
- TASK/진행/작업 로그 문서 동기화

## 3. 작업 제외 범위

- 챗봇 처리 플로우(비즈니스 로직) 변경
- API 스펙/엔드포인트 변경
- 인증/권한/세션 정책 변경
- DB schema/entity 변경

## 4. 완료 기준

- [x] LLM 시스템 프롬프트가 한국어로 정리되었다.
- [x] chatbot 도메인 주요 클래스/메서드에 상세 주석이 반영되었다.
- [x] `./gradlew compileJava` 성공
- [x] `./gradlew test --tests "*ChatbotServiceTest" --tests "*ChatbotControllerTest"` 성공
- [x] `./gradlew test` 성공

## 5. 예상 영향 범위

- 대상 레이어: chatbot domain(main code)
- 영향: 코드 가독성/온보딩 효율 개선
- 사용자 기능 영향: 없음(동작 로직 유지)

## 6. 위험도

- 등급: LOW
- 근거: 기능 변경 없이 프롬프트 문구/주석 중심 작업

## 7. 확인 필요 여부

- 필요 여부: 아니오
- 사유: 공개 API/설정/DB 변경 없음

## 8. 작업 Workflow

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 대상 파일 분석 | chatbot 도메인 파일 목록 | 수정 대상 확정 | 파일 누락 여부 확인 |
| 2 | 코드 반영 | 기존 코드 | 한글 주석 + 한글 프롬프트 반영 코드 | `./gradlew compileJava` |
| 3 | 회귀 검증 | 반영 코드 | 테스트 통과 결과 | `./gradlew test ...`, `./gradlew test` |
| 4 | 문서 동기화 | 작업 결과 | TASK/PROGRESS/WORKLOG 업데이트 | 문서 내용 확인 |

## 9. 검증 결과

- `./gradlew compileJava` 성공
- `./gradlew test --tests "*ChatbotServiceTest" --tests "*ChatbotControllerTest"` 성공
- `./gradlew test` 성공

## 10. 결과 기록

- 변경 파일:
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotController.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
  - `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotErrorResponse.java`
  - `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotException.java`
  - `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotExceptionHandler.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmPlan.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
  - `.docs/.task/.KHJ/flow/TASK15.md`
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- 미완료 항목(TODO): 없음
- 리스크/주의사항: 없음
