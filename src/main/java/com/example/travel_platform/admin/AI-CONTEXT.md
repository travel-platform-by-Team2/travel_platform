<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 페이지의 SSR 진입 라우트와 화면 연결을 담당한다.

## 주요 파일

| 파일명               | 설명                                                                                                                                        |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| AdminController.java | `/admin`, `/admin/users`, `/admin/lodgings`, `/admin/boards` 요청을 받아 관리자 Mustache 페이지를 렌더링하고 사이드바 활성 상태를 세팅한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `admin` 패키지는 새 비즈니스 엔티티 도메인이 아니라 관리자 화면 진입 패키지로 유지한다.
- 관리자 권한 검사는 컨트롤러 안에서 중복 구현하지 말고 `_core/filter/AdminFilter`의 `/admin`, `/admin/*` 보호를 재사용한다.
- 관리자 하위 페이지는 같은 패키지의 SSR 컨트롤러에서 라우트를 확장하고, 현재는 사이드바 활성 상태와 정적 페이지 렌더링만 담당한다.
- 공통 왼쪽 패널은 현재 `대시보드`, `유저 관리`, `게시글 관리` 3개 메뉴만 노출하며, `숙소 관리` 경로는 라우트는 남아 있어도 사이드바 메뉴에서는 제외된 상태다.
- 유저/숙소/게시글 관리 페이지의 액션 패널은 더미 UI 인터랙션이며 실제 처리 로직과 분리해 유지한다.

## 테스트

- 관리자 로그인 상태에서 `/admin`, `/admin/users`, `/admin/lodgings`, `/admin/boards`가 각각 올바른 `pages/admin-*` 템플릿을 렌더링하는지 확인한다.
- 사이드바 활성 메뉴, 대시보드 `전체보기` 링크, 액션 패널 UI 동작을 함께 점검한다.
- 비관리자 또는 비로그인 상태에서 `/admin*` 접근 시 관리자 필터가 차단하는지 함께 점검한다.

## 의존성

- 내부: `_core/filter`, `src/main/resources/templates/partials/admin-sidebar.mustache`, `src/main/resources/templates/partials/admin-page-scripts.mustache`, `src/main/resources/templates/pages/admin-dashboard.mustache`, `src/main/resources/templates/pages/admin-users.mustache`, `src/main/resources/templates/pages/admin-lodgings.mustache`, `src/main/resources/templates/pages/admin-boards.mustache`
- 외부: `Spring MVC`
