<!-- Parent: ../AI-CONTEXT.md -->

# handler

## 목적

전역 예외 처리와 공통 오류 응답 포맷을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| ApiErrorResponse.java | API 오류 응답 포맷을 정의한다. |
| ApiExceptionHandler.java | `@RestController` 대상 API 예외를 처리한다. |
| GlobalExceptionHandler.java | 전역 예외를 처리한다. |

## 하위 디렉토리

- `ex/` - 재사용 예외 타입을 둔다.

## AI 작업 지침

- MVC 화면용 예외 처리와 JSON API용 예외 처리가 섞이지 않도록 현재 분리를 유지한다.
- 오류 응답 필드 이름을 바꾸면 API 소비 코드와 테스트를 같이 갱신한다.

## 테스트

- 예외 응답 형태를 바꾼 경우 실패 케이스에 대한 API/MVC 동작을 모두 확인한다.

## 의존성

- 내부: `ex`, 각 도메인 서비스
- 외부: `Spring MVC`
