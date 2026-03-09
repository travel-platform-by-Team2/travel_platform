# TASK14 - 챗봇 스토리보드 실구현 정렬

## 0. 작업 유형

- 기능 추가

## 1. 작업 목표

- 스토리보드 기준 챗봇 처리 흐름을 실제 코드에 정렬한다.
- LLM 1차 계획(JSON) + DB 조회 + LLM 2차 답변(JSON) 구조를 고정한다.

## 2. 작업 범위

- `ChatbotService` 흐름 정렬 및 서버 스키마 컨텍스트 전달
- `ChatbotLlmClient` 계약 확장(스키마 컨텍스트/2차 JSON 응답)
- `OpenAiChatbotLlmClient` 프롬프트/파싱 강화
- `ChatbotServiceTest` 갱신
- TASK/진행/워크로그 문서 동기화

## 3. 작업 제외 범위

- 인증/세션/필터 정책 변경
- SQL 보안 정책 강화
- 프론트엔드 변경
- DB schema/entity 변경

## 4. 완료 기준

- [x] 서버가 LLM 1차 계획 요청 시 스키마 컨텍스트를 함께 전달한다.
- [x] LLM 2차 답변은 JSON(`{"answer":"..."}`) 기반으로 파싱된다.
- [x] `needsDb=false`/`needsDb=true` 분기 동작이 테스트로 검증된다.
- [x] `./gradlew test --tests "*ChatbotServiceTest"` 통과

## 5. 예상 영향 범위

- 대상 레이어: chatbot service/llm/test
- 대상 기능: 질문 처리 오케스트레이션
- 사용자 영향: 답변 생성 품질/일관성 개선

## 6. 위험도

- 등급: MEDIUM
- 근거: 핵심 처리 흐름과 내부 계약(JSON) 변경

## 7. 승인 필요 여부

- 필요 여부: 아니오
- 승인 필요 사유: API 엔드포인트 계약은 유지, 내부 처리 정렬

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | 내부 계약 확정 | 스토리보드 + API 스펙 | LLM client 인터페이스 변경안 | 시그니처 점검 |
| 2 | 서비스/클라이언트 구현 | 기존 chatbot 코드 | 정렬된 오케스트레이션 코드 | `./gradlew compileJava` |
| 3 | 테스트/문서 동기화 | 변경 코드 | 테스트/문서 반영 | `./gradlew test --tests "*ChatbotServiceTest"` |

### 8.2 중단 조건

- 범위 외 공개 API 스펙 변경이 필요한 경우
- 인증/세션 정책 선반영이 필수인 경우

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`
- 테스트:
  - `./gradlew test --tests "*ChatbotServiceTest"`
  - `./gradlew test`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `.docs/.task/.KHJ/flow/TASK14.md`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
  - `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- 테스트 결과:
  - `./gradlew compileJava` 성공
  - `./gradlew test --tests "*ChatbotServiceTest"` 성공
  - `./gradlew test` 성공
- 미완료 항목(TODO):
  - SQL 보안 정책(검증/화이트리스트) 적용
  - 인증/세션 기반 정책 연동
- 리스크/주의사항:
  - LLM이 JSON 포맷을 준수하지 않으면 내부 오류로 처리됨
