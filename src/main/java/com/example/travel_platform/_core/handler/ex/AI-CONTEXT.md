<!-- Parent: ../AI-CONTEXT.md -->

# ex

## 목적

서비스와 컨트롤러에서 공통으로 사용하는 예외 타입을 정의한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| ApiException.java | API 오류 코드와 상태를 담는 공통 예외다. |
| StatusException.java | HTTP 상태 기반 공통 예외의 부모 타입이다. |
| Exception400.java | 잘못된 요청용 예외다. |
| Exception401.java | 인증 실패용 예외다. |
| Exception403.java | 권한 부족용 예외다. |
| Exception404.java | 리소스 없음 예외다. |
| Exception500.java | 서버 오류용 예외다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 새 예외를 추가할 때는 어떤 핸들러가 처리할지까지 같이 정의한다.
- 도메인 메시지는 예외 타입보다 서비스 계층에서 더 구체적으로 유지한다.
- `Exception400~500`은 `StatusException`을 상속하고, `ApiException`은 API 전용 code/status를 함께 담는 예외로 구분한다.

## 테스트

- 예외를 새로 도입했다면 관련 컨트롤러 응답 상태와 메시지를 확인한다.
- 공통 예외 구조를 바꾸면 `ExceptionHandlerTest`, API controller 테스트를 같이 확인한다.

## 의존성

- 내부: `_core/handler`, 각 도메인 서비스
- 외부: `Spring MVC`
