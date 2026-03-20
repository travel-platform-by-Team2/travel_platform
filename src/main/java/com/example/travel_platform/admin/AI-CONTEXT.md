<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 페이지의 SSR 진입 라우트, 대시보드 요약 데이터, 유저/게시글 관리 화면 연결을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| AdminController.java | `/admin`, `/admin/users`, `/admin/boards`, `/admin/boards/{boardId}/delete` 요청을 받아 관리자 Mustache 페이지를 렌더링하거나 삭제 redirect 를 처리한다. |
| AdminService.java | 관리자 대시보드, 유저 관리, 게시글 관리 3개 페이지 기준으로 서비스 메서드를 나눠 조회와 화면 DTO 조립을 담당한다. |
| AdminRepository.java | 관리자 화면에서 재사용하는 사용자 조회와 집계 쿼리를 담당한다. |
| AdminResponse.java | 대시보드, 유저 관리, 게시글 관리 화면 DTO를 정의한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `admin` 패키지는 새 비즈니스 엔티티 도메인이 아니라 관리자 화면 진입과 관리자용 조회 조립 패키지로 유지한다.
- 관리자 권한 검사는 컨트롤러 안에서 중복 구현하지 말고 `_core/interceptor/AdminInterceptor`의 `/admin`, `/admin/*` 보호를 재사용한다.
- `admin`은 현재 페이지 3장(`/admin`, `/admin/users`, `/admin/boards`) 기준으로 유지하며, 파일을 과하게 쪼개기보다 `AdminService` 내부 메서드 책임을 페이지 단위로 읽기 쉽게 정리하는 방향을 우선한다.
- `AdminController`는 `renderDashboardPage(...)`, `renderUsersPage(...)`, `renderBoardsPage(...)` helper로 페이지 진입과 삭제 redirect 흐름을 분리했다.
- `/admin` 대시보드는 현재 실데이터 기반 요약 화면이며, `AdminService.getDashboardPage()`가 유저/게시글 요약 카드, 그래프 2개, 최근 유저/최근 게시글 3건을 조립한다.
- `/admin/users`는 `AdminService.getUsersPage(...)`가 목록, 카운트, 탭 상태를 한 번에 조립하고, 컨트롤러는 `page` 루트 하나만 템플릿에 넘긴다.
- `/admin/boards`는 `AdminService.getBoardsPage(...)`가 목록, 페이지네이션, 카테고리 선택 상태를 조립한다.
- 관리자 3개 페이지 템플릿은 모두 `page` 루트 계약을 사용하고, `AdminResponse.*PageDTO.withCurrentMenu(...)`가 사이드바 활성 상태를 채운다.
- `AdminService.deleteBoard(...)`는 `requireAdmin(...)`, `findBoard(...)`, `deleteBoardRelations(...)` helper 기준으로 읽히게 정리됐다.
- `AdminResponse.AdminBoardDTO`의 날짜 필드는 `createdDate`를 사용한다.
- 대시보드 데이터는 현재 코드베이스에서 바로 구할 수 있는 유저/게시글 범위만 사용하며, 차트 라이브러리 없이 Mustache + CSS 기반 막대/비율형 시각화로 표현한다.
- 공통 왼쪽 패널은 현재 `대시보드`, `유저 관리`, `게시글 관리` 3개 메뉴만 노출한다.
- `admin-users`, `admin-boards` 페이지는 기존 화면 구조를 유지하되 `page` 루트로만 값을 읽는다.
- 유저/게시글 관리 페이지의 액션 패널은 더미 UI 인터랙션이며 실제 처리 로직과 분리해 유지한다.
- `숙소 관리`는 현재 관리자 라우트와 템플릿 운영 범위에서 제외된 상태이며, 이번 리팩토링에서도 실구현하지 않는다.

## 테스트

- `AdminControllerTest`에서 대시보드, 유저 관리, 게시글 관리, 게시글 삭제 redirect 흐름을 직접 호출 방식으로 확인한다.
- `AdminServiceDashboardTest`에서 대시보드, 유저 목록, 게시글 목록 조립 결과를 실제 데이터 기준으로 확인한다.
- `AdminServiceTest`에서 게시글 삭제의 `401/403/404/정상` 흐름과 게시글 목록 조회 경로를 mock 기반으로 확인한다.
- `AdminTemplateContractTest`에서 `admin-dashboard`, `admin-users`, `admin-boards`가 `page` 루트와 `createdDate` 계약을 읽는지 확인한다.
- 비관리자 또는 비로그인 상태에서 `/admin*` 접근 시 관리자 필터가 차단하는지는 인터셉터와 컨트롤러 삭제 테스트를 함께 본다.
- `./gradlew.bat test`로 자동 테스트를 확인한다.

## 의존성

- 내부: `_core/interceptor`, `../board/BoardRepository.java`, `../user/User.java`, `src/main/resources/templates/partials/admin-sidebar.mustache`, `src/main/resources/templates/partials/admin-page-scripts.mustache`, `src/main/resources/templates/pages/admin-dashboard.mustache`, `src/main/resources/templates/pages/admin-users.mustache`, `src/main/resources/templates/pages/admin-boards.mustache`, `src/main/resources/static/css/admin.css`
- 외부: `Spring MVC`, `Spring Data JPA`
