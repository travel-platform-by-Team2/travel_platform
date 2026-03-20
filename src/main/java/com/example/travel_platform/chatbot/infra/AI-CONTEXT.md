<!-- Parent: ../AI-CONTEXT.md -->

# infra

## 목적

챗봇의 외부 시스템 연동 구현체를 두는 인프라 계층이다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| 없음 | 이 디렉토리는 하위 LLM 연동 패키지를 묶는 루트다. |

## 하위 디렉토리

- `llm/` - OpenAI 기반 LLM 연동 구현을 둔다.

## AI 작업 지침

- 외부 연동 설정은 환경 변수로 주입받고 코드에 비밀값을 넣지 않는다.
- 네트워크 오류와 응답 파싱 오류를 상위 계층이 처리할 수 있는 형태로 유지한다.
- infra 변경 시 application 예외 경계와 챗봇 JSON 응답 파싱 helper를 같이 확인한다.

## 테스트

- 외부 연동 포맷 변경 시 infra parsing 테스트와 오케스트레이션 테스트까지 함께 확인한다.

## 의존성

- 내부: `chatbot/application`, `_core/handler/ex`
- 외부: `OpenAI Responses API`
