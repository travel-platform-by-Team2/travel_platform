<!-- Parent: ../AI-CONTEXT.md -->

# _core

## 목적

로그인/관리자 접근 제어, 예외 처리, 공통 응답 등 여러 도메인에서 공유하는 횡단 관심사를 관리한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| 없음 | 이 디렉토리는 하위 공통 패키지를 묶는 루트다. |

## 하위 디렉토리

- `interceptor/` - 로그인 인터셉터, 관리자 인터셉터, 인터셉터 등록 설정을 둔다.
- `handler/` - 전역 예외 처리와 오류 응답 포맷을 둔다.
- `util/` - 공용 유틸리티를 둔다.
- `validation/` - 공통 커스텀 validation 어노테이션과 validator를 둔다.

## AI 작업 지침

- 이 계층에는 도메인별 비즈니스 규칙을 넣지 않는다.
- 인터셉터나 예외 처리 변경은 게시판, 캘린더, 관리자 라우트, 챗봇 전체에 영향을 줄 수 있음을 전제로 수정한다.
- 인증 정책을 바꿀 때는 `sessionUser` 세션 키와 `User.role` 기반 관리자 판별이 같이 맞는지 확인한다.
- `handler`, `interceptor`, `util`, `validation`, `handler/ex`는 각각 예외 처리, 접근 제어, 공통 응답, 공통 검증, 예외 타입 역할을 유지한다.
- 공통 계층 리팩토링 후에는 부분 테스트만 보지 말고 `./gradlew.bat test` 전체 기준선까지 확인한다.
- 공통 템플릿/정적 자산 계약 검증은 `src/test/java/com/example/travel_platform/_core/template` 하위 테스트에서 관리하므로, partial 구조를 바꿀 때 관련 테스트도 같이 맞춘다.

## 테스트

- 관련 변경 후 `./gradlew.bat test`로 전체 영향 범위를 확인한다.
- 공통 partial 구조 변경 시 `StaticScriptContractTest`, `StaticStyleContractTest`를 함께 확인한다.

## 의존성

- 내부: `interceptor`, `handler`, `util`, `validation`, 각 도메인 패키지
- 외부: `Spring MVC`, `Jakarta Servlet`
