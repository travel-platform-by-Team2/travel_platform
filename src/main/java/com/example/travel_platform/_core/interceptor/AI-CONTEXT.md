<!-- Parent: ../AI-CONTEXT.md -->

# interceptor

## 목적

Spring MVC 요청에서 로그인 여부, 관리자 권한, 로그인 중 비활성 사용자 차단을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| AdminInterceptor.java | `/admin`, `/admin/*` 요청에서 관리자 권한을 검사하고, 로그인 중 비활성 처리된 일반 사용자는 로그인 페이지로 강제 로그아웃시킨다. |
| LoginInterceptor.java | 로그인 여부를 검사하고, 로그인 중 비활성 처리된 일반 사용자를 API/MVC 요청별로 다르게 차단한다. |
| WebMvcConfig.java | 로그인 인터셉터와 관리자 인터셉터를 URL 패턴별로 등록한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 세션 키 이름과 로그인 예외 경로를 바꿀 때는 `user` 패키지와 헤더 UI까지 같이 확인한다.
- 인증 로직을 추가하더라도 컨트롤러 안에 중복 체크를 늘리기보다 인터셉터 경계에서 처리한다.
- 현재 `WebMvcConfig`는 `LoginInterceptor`를 page 경로와 API 경로 배열로 분리해 등록하고, `AdminInterceptor`를 `/admin`, `/admin/**`에 등록한다.
- 현재는 `GET /boards/{id}`만 비로그인 상세 조회 예외가 있고, `/api/*` 미인증 요청은 redirect 대신 401 상태만 반환한다.
- 비활성 일반 사용자 세션은 `user/UserSessionChecker.java`를 통해 DB의 최신 `active` 상태를 기준으로 검사한다. 일반 페이지 요청은 alert 스크립트와 `/login-form` redirect, `/api/*` 요청은 세션 종료 후 403 상태로 처리한다.
- 관리자 계정은 비활성 일반 사용자 차단 예외로 유지한다.

## 테스트

- 로그인/비로그인 상태로 접근 제어가 달라지는 화면과 API를 함께 점검한다.
- `GET /boards/{id}` 예외 통과, 일반 MVC redirect, `/api/calendar*` 401 반환, `/admin`, `/admin/*` 비관리자 차단이 각각 유지되는지 확인한다.
- 비활성 일반 사용자가 로그인 시 차단되고, 로그인 중 비활성 처리되면 일반 요청에서는 `/login-form`으로 강제 이동되며 `/api/*` 요청은 403이 반환되는지 확인한다.
- `InterceptorBehaviorTest`, `WebMvcConfigPathCoverageTest`로 동작과 경로 등록을 같이 확인한다.

## 의존성

- 내부: `user`, `_core/handler`, `_core/util`
- 외부: `Spring MVC`
