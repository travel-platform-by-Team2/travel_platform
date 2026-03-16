<!-- Parent: ../AI-CONTEXT.md -->

# filter

## 목적

세션 기반 로그인 검사, 관리자 권한 검사, 필터 등록 설정을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| AdminFilter.java | `/admin`, `/admin/*` 요청에서 `sessionUser.role == ADMIN` 여부를 검사한다. |
| FilterConfig.java | 로그인 필터와 관리자 필터를 URL 패턴별로 등록하고 실행 순서를 지정한다. |
| LoginFilter.java | 로그인 여부를 검사하고 API/MVC 요청을 다르게 처리하는 필터 구현이다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 세션 키 이름과 로그인 예외 경로를 바꿀 때는 `user` 패키지와 헤더 UI까지 같이 확인한다.
- 인증 로직을 추가하더라도 컨트롤러 안에 중복 체크를 늘리기보다 필터 경계에서 처리한다.
- 현재 `FilterConfig`는 `LoginFilter`를 `/boards/*`, `/calendar`, `/calendar/*`, `/api/calendar`, `/api/calendar/*`에, `AdminFilter`를 `/admin`, `/admin/*`에 등록한다.
- 현재는 `GET /boards/{id}`만 비로그인 상세 조회 예외가 있고, `/api/*` 미인증 요청은 redirect 대신 401 상태만 반환한다.
- 관리자 필터는 비로그인 또는 일반 사용자 접근 시 alert 스크립트와 `/` redirect를 반환하므로, 관리자 API를 추가할 때는 응답 타입 차이를 먼저 정리해야 한다.

## 테스트

- 로그인/비로그인 상태로 접근 제어가 달라지는 화면과 API를 함께 점검한다.
- `GET /boards/{id}` 예외 통과, 일반 MVC redirect, `/api/calendar*` 401 반환, `/admin`, `/admin/*` 비관리자 차단이 각각 유지되는지 확인한다.

## 의존성

- 내부: `user`, `_core/handler`
- 외부: `Spring MVC`, `Jakarta Servlet`
