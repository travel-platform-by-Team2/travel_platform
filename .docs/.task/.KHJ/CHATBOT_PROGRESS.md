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

### 1.2 미완료 항목
1. 백엔드 챗봇 API 구현 미완료
   - 4-1~4-3은 완료되었으나 DB 조회/최종 답변 로직은 미구현

2. 테스트 미구현
   - 챗봇 도메인 단위/통합 테스트 없음

## 2. 단계별 진행 현황

| 단계 | 작업 내용 | 상태 | 비고 |
| --- | --- | --- | --- |
| 1단계 | UI 스켈레톤(파셜/스타일) | 완료 | 현재 코드에 반영됨 |
| 2단계 | 프론트 기본 인터랙션(열기/닫기) | 완료 | `chatbot.js`에 반영 |
| 3단계 | 메시지 송수신 프론트 로직 | 완료 | fetch 연동 및 응답/에러 렌더링 반영 |
| 4단계 | 백엔드 챗봇 API 설계/구현 | 진행중 | 4-1~4-3 완료 / 4-4~4-6 미진행 |
| 5단계 | 대화 저장 모델/영속성 | 제외 | 요구사항: 비저장 원칙 |
| 6단계 | 권한/세션 연동 | 보류 | 경로/정책 확정 후 적용 |
| 7단계 | 테스트 및 검증 자동화 | 미진행 | 테스트 코드 작성 필요 |

## 3. 다음 작업 제안 순서
1. 4-4 SQL 생성 및 DB 조회 단계 구현
2. 4-5 최종 답변 생성 단계 구현
3. 4-6 예외/응답 형식 정렬
4. 통합 테스트 및 예외 케이스 테스트 추가

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

## 4. 리스크 및 의존성
1. 인증/필터 경로 정책 미확정
2. SQL 생성/실행 보안 정책 미적용 상태(마무리 단계 적용 예정)
3. 페이지 갱신 시 문맥 유실로 인한 사용자 경험 저하 가능성

