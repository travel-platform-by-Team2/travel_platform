<!-- Parent: ../AI-CONTEXT.md -->

# dto

## 목적

챗봇 API의 요청과 응답 DTO를 정의한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| ChatbotRequest.java | 챗봇 요청 DTO를 정의한다. |
| ChatbotResponse.java | 챗봇 응답 DTO를 정의한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 필드 이름이나 검증 규칙을 바꾸면 컨트롤러 테스트와 프런트엔드 요청 형태를 같이 수정한다.
- LLM 메타데이터 필드는 응답 소비 코드가 기대하는 구조를 유지한다.

## 테스트

- DTO 검증과 직렬화/역직렬화 테스트를 함께 확인한다.

## 의존성

- 내부: `chatbot/api`, `chatbot/application`
- 외부: `Jakarta Validation`
