# 챗봇 개발 진행 단계 문서

## 0. 문서 정보
- 작성일: 2026-03-04
- 작성자: KHJ 협업
- 기준 문서: `.docs/.task/.KHJ/AGENT_KHJ.md`
- 목적: 챗봇 시스템 개발 진행 단계를 현재 코드 기준으로 문서화

## 1. 현재 구현 상태 요약

### 1.1 완료된 항목
1. 챗봇 UI 파셜 구성 완료
   - 파일: `src/main/resources/templates/partials/chatbot.mustache`
   - 내용: 플로팅 버튼, 챗봇 패널, 샘플 메시지, 입력 폼

2. 챗봇 기본 UI 동작(JS) 완료
   - 파일: `src/main/resources/static/js/chatbot.js`
   - 내용: 열기/닫기, ESC 닫기, 외부 클릭 닫기

3. 챗봇 스타일(CSS) 분리 완료
   - 파일: `src/main/resources/static/css/chatbot.css`
   - 내용: FAB, 패널, 메시지 버블, 폼 레이아웃 스타일

4. 페이지 공통 포함 적용 완료
   - `{{>partials/chatbot}}`가 주요 페이지에 포함되어 공통 노출됨

5. 챗봇 API 스펙 초안 작성 완료
   - 파일: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
   - 내용: 질문 처리 API, 요청/응답, 에러 규격 초안 정의

6. 챗봇 처리 흐름(분류/SQL/재질문) 확정
   - DB 필요 여부 분기 + SQL 생성/조회 + 최종 자연어 응답 흐름 확정
   - 보안 검증은 개발 마무리 단계에서 후순위 적용 원칙 확정

7. 인증/필터 적용 보류 원칙 확정
   - 현재 단계에서는 인증/인가 미적용
   - 필터 적용 경로는 미확정 상태로 후속 결정

8. 대화 비저장 원칙 확정
   - 페이지 새로고침 시 대화 내용 초기화
   - 서버/DB에 대화 로그 저장하지 않음

9. 3단계 프론트 메시지 송수신 로직 구현 완료
   - 파일: `src/main/resources/static/js/chatbot.js`
   - 반영: 입력/전송 이벤트, API 호출(fetch), 응답 렌더링, 실패 안내 메시지

10. 4단계 세분화 문서 작성 완료
   - 파일: `.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`
   - 반영: 4-1 ~ 4-6 구현 단위, 완료 기준, 검증 계획, 보류 항목 정의

11. 4-1 API 엔드포인트 뼈대 구성 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotController.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
   - 반영: `POST /api/chatbot/messages` 라우팅 및 DTO 검증 연결

12. 4-2 서비스 내부 책임 분리 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 반영: 오케스트레이터 없이 서비스 내부 단계 분리 구조로 정리

13. 4-3 질문 분류 단계 구현 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 반영: `needsDb/reason/queryIntent` 분류 결과 산출 및 `DIRECT_LLM/DB_QUERY` 분기 처리

14. 4-4 SQL 생성 및 DB 조회 단계 구현 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 반영: `queryIntent`별 SQL 계획 생성, `JdbcTemplate` 기반 조회, `rowCount` 산출

15. 4-5 최종 답변 생성 단계 구현 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
   - 반영: `DIRECT_LLM`/`DB_QUERY` 분기별 답변 생성, `meta` 확장(`querySummary/generatedSql/rowCount`)

16. 4-6 예외/응답 형식 정렬 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotException.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotErrorResponse.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotExceptionHandler.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
   - 반영: 챗봇 API 전용 JSON 오류 응답(`CHATBOT_BAD_REQUEST`, `CHATBOT_INTERNAL_ERROR`) 적용

17. 챗봇 서비스 단위 테스트 추가 완료
   - 파일: `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
   - 반영: DIRECT_LLM 분기, DB_QUERY 분기, DB 오류 예외 케이스 검증

18. 챗봇 컨트롤러/예외 처리 통합 테스트 추가 완료
   - 파일: `src/test/java/com/example/travel_platform/chatbot/ChatbotControllerTest.java`
   - 반영: 정상 응답(200), 입력 검증 오류(400), 내부 오류(500), JSON 파싱 오류(400) 검증

19. 챗봇 빈 메시지 입력 검증 회귀 수정 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotController.java`
   - 반영: `@NotBlank` + `@Valid` 적용으로 빈 `message` 요청 400 응답 복구
   - 검증: `./gradlew test --tests "*ChatbotControllerTest"` 및 `./gradlew test` 통과

20. 챗봇 OpenAI LLM 연동(Direct 경로) 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 반영: `DIRECT_LLM` 경로가 OpenAI Responses API(`/v1/responses`) 호출로 응답 생성
   - 반영: OpenAI 요청/응답 JSON 처리를 Gson 기반으로 적용
   - 반영: OpenAI 호출 실패/응답 파싱 실패 시 `CHATBOT_INTERNAL_ERROR` 처리
   - 검증: `./gradlew test --tests "*ChatbotServiceTest"`, `./gradlew test` 통과

21. 챗봇 LLM 중심 처리 흐름 리팩토링 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmPlan.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
   - 반영: 키워드 분류/고정 SQL/고정 답변 로직 제거
   - 반영: LLM이 `needsDb/queryIntent/querySummary/sql/answer` 계획을 생성하고 서비스가 실행
   - 반영: DB 조회 결과 기반 최종 답변도 LLM이 생성
   - 검증: `./gradlew test --tests "*ChatbotServiceTest"`, `./gradlew test` 통과

22. 스토리보드 기반 JSON 스키마 고정 문서화 완료
   - 파일: `.docs/.task/.KHJ/CHATBOT_API_SPEC.md`
   - 반영: 외부 API 요청/응답 스키마 고정
   - 반영: 내부 LLM 계약을 1차 계획/2차 답변 단계로 분리 명시
   - 반영: `needsDb=false`/`needsDb=true` 분기별 필수 필드 규칙 문서화
   - 검증: 문서 리뷰 완료

23. 스토리보드 실구현 정렬 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
   - 파일: `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
   - 반영: 서버가 LLM 1차 계획 호출 시 스키마 컨텍스트 전달
   - 반영: LLM 2차 답변을 JSON(`answer`) 기반으로 파싱
   - 검증: `./gradlew test --tests "*ChatbotServiceTest"`, `./gradlew test` 통과

24. 챗봇 도메인 가독성 정리(프롬프트 한글화/주석 보강) 완료
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotController.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotRequest.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotResponse.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotErrorResponse.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotException.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/exception/ChatbotExceptionHandler.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmPlan.java`
   - 파일: `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
   - 반영: OpenAI 시스템 프롬프트를 한국어로 통일
   - 반영: 클래스/필드/메서드 주석을 상세화해 코드 이해도 개선
   - 검증: `./gradlew compileJava`, `./gradlew test --tests "*ChatbotServiceTest" --tests "*ChatbotControllerTest"`, `./gradlew test` 통과

25. OpenAI 키 `.env` 외부화 완료
   - 파일: `src/main/resources/application.properties`
   - 파일: `.gitignore`
   - 파일: `.env.example`
   - 반영: `spring.config.import=optional:file:.env[.properties]`로 `.env` 자동 로드
   - 반영: `.env`, `.env.*` git 추적 제외 및 `.env.example` 공유 템플릿 추가
   - 검증: `./gradlew compileJava`, `./gradlew test --tests "*ChatbotServiceTest" --tests "*ChatbotControllerTest"` 통과

### 1.2 미완료 항목
1. SQL 보안 정책 미적용
   - 화이트리스트/검증/권한 제어는 후속 단계

2. 인증/필터 경로 정책 미확정
   - 챗봇 API 인증/인가 적용 범위는 후속 단계에서 확정 필요

## 2. 단계별 진행 현황

| 단계 | 작업 내용 | 상태 | 비고 |
| --- | --- | --- | --- |
| 1단계 | UI 스켈레톤(파셜/스타일) | 완료 | 현재 코드에 반영됨 |
| 2단계 | 프론트 기본 인터랙션(열기/닫기) | 완료 | `chatbot.js`에 반영 |
| 3단계 | 메시지 송수신 프론트 로직 | 완료 | fetch 연동 및 응답/에러 렌더링 반영 |
| 4단계 | 백엔드 챗봇 API 설계/구현 | 완료 | 4-1~4-6 완료(2026-03-05) |
| 5단계 | 대화 저장 모델/영속성 | 제외 | 요구사항: 비저장 원칙 |
| 6단계 | 권한/세션 연동 | 보류 | 경로/정책 확정 후 적용 |
| 7단계 | 테스트 및 검증 자동화 | 진행중 | 챗봇/전체 테스트 통과, 보안·정책 후속 반영 필요 |

## 3. 다음 작업 제안 순서
1. SQL 실행 보안 정책(검증/화이트리스트) 설계 및 적용
2. 인증/필터 경로 정책 확정 후 챗봇 API 연동
3. 보안/예외 경계 케이스 테스트 보강
4. 코드컨벤션 기준 구조 리팩토링 후속 적용

완료 이력:
- 2026-03-04: 챗봇 API 스펙 초안 작성 완료 (`.docs/.task/.KHJ/CHATBOT_API_SPEC.md`)
- 2026-03-04: 챗봇 처리 흐름 확정 (DB 필요 여부 분기 + SQL 생성/실행 + 최종 응답)
- 2026-03-04: 인증/필터 적용 보류 원칙 확정
- 2026-03-04: 대화 비저장 원칙 확정 (프론트 표시 전용)
- 2026-03-04: 3단계 프론트 메시지 송수신 로직 구현 완료
- 2026-03-04: 4단계 세분화 문서 작성 완료 (`.docs/.task/.KHJ/CHATBOT_STAGE4_BREAKDOWN.md`)
- 2026-03-04: 4-1 API 엔드포인트 뼈대 구성 완료
- 2026-03-04: 4-2 서비스 내부 책임 분리 완료
- 2026-03-04: 4-3 질문 분류 단계 구현 완료
- 2026-03-05: 4-4 SQL 생성 및 DB 조회 단계 구현 완료
- 2026-03-05: 4-5 최종 답변 생성 단계 구현 완료
- 2026-03-05: 4-6 예외/응답 형식 정렬 완료
- 2026-03-05: 챗봇 서비스 단위 테스트 추가 완료 (`ChatbotServiceTest`)
- 2026-03-05: 챗봇 컨트롤러/예외 처리 통합 테스트 추가 완료 (`ChatbotControllerTest`)
- 2026-03-05: 빈 `message` 입력 검증 회귀 수정 완료 (`@NotBlank`, `@Valid`)
- 2026-03-05: OpenAI 기반 `DIRECT_LLM` 연동 완료 (`ChatbotLlmClient`, `OpenAiChatbotLlmClient`)
- 2026-03-05: LLM 중심 처리 흐름 리팩토링 완료 (`ChatbotLlmPlan`, LLM plan 기반 서비스 분기)
- 2026-03-05: 스토리보드 기준 JSON 스키마 고정 (`CHATBOT_API_SPEC v0.3-draft`)
- 2026-03-05: 스토리보드 실구현 정렬 완료 (스키마 컨텍스트 전달 + 2차 JSON 답변 파싱)
- 2026-03-05: 전체 테스트 `./gradlew test` 통과 확인
- 2026-03-05: 챗봇 프롬프트 한글화 및 도메인 주석 보강 완료 (`chatbot/**`, `OpenAiChatbotLlmClient`)
- 2026-03-05: OpenAI 키 `.env` 외부화 완료 (`application.properties`, `.gitignore`, `.env.example`)

## 4. 리스크 및 의존성
1. 인증/필터 경로 정책 미확정
2. SQL 생성/실행 보안 정책 미적용 상태(마무리 단계 적용 예정)
3. 페이지 갱신 시 문맥 유실로 인한 사용자 경험 저하 가능성
4. 입력 검증 어노테이션 제거/누락 시 빈 메시지 200 회귀 가능
5. OpenAI 키(`OPENAI_API_KEY`)가 `.env` 또는 런타임 환경변수에 미설정 시 `DIRECT_LLM` 요청이 실패함

