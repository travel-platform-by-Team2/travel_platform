<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 페이지의 SSR 진입 흐름과 대시보드, 사용자 관리, 게시글 관리 화면 조립을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `AdminController.java` | `/admin`, `/admin/users`, `/admin/boards`, `/admin/boards/{boardId}/delete` 요청을 받아 관리자 Mustache 페이지를 렌더링하고 redirect query를 유지한다. |
| `AdminService.java` | 관리자 화면에 필요한 조회 결과를 DTO로 조립한다. |
| `AdminQueryRepository.java` | 관리자 화면에서 사용하는 사용자/게시글 조회와 대시보드 집계를 한 곳에서 담당한다. |
| `AdminResponse.java` | 관리자 화면 DTO를 정의한다. |

## 현재 구조 기준

- `admin`은 자체 엔티티를 소유하는 패키지가 아니라 `user`, `board`를 엮어 관리자 화면을 조립하는 도메인이다.
- 조회는 `AdminQueryRepository` 하나로 모아두고 JPQL 기반 row 조회를 사용한다.
- `select new ...` 생성자 표현식 대신 `Tuple` alias 조회 후 row 매핑 방식으로 정리했다.
- 사용자/게시글 keyword 검색은 `lower(...)` 기준으로 대소문자 처리를 통일했다.
- 액션은 기존 도메인 repository를 사용한다.
  - 사용자 상태 변경: `UserRepository`
  - 게시글 삭제: `BoardRepository`, `BoardLikeRepository`
- 게시글 삭제 후에는 현재 `category`, `keyword`, `sort`, `page` query를 유지한 채 `/admin/boards`로 redirect 한다.
- `AdminController`는 `renderDashboardPage(...)`, `renderUsersPage(...)`, `renderBoardsPage(...)` helper로 화면 진입을 분리한다.
- `/admin`은 `model` 단건 루트 계약을 사용한다.
- `/admin/users`, `/admin/boards`는 `model + models` 루트 계약을 사용한다.
- `AdminResponse` 정적 팩토리 메서드는 `createDashboardView`, `createUserListView`, `createBoardListView`, `createAdminUser`처럼 역할이 드러나는 이름을 사용한다.
- `AdminUserDTO`에서 현재 화면에 쓰지 않는 `statusText`, `managementLabel`은 제거했다.
- 숙소 관리 기능은 현재 범위에서 제외했고 `admin-lodgings.mustache`는 제거했다.

## 테스트

- `AdminControllerTest`: 관리자 페이지 진입, 사용자 상태 변경 redirect, 게시글 삭제 query 유지
- `AdminServiceDashboardTest`: 대시보드/사용자/게시글 조립 결과
- `AdminServiceTest`: 사용자 상태 변경, 게시글 삭제, 목록 조립, 오류 흐름
- `AdminQueryRepositoryTest`: 사용자/게시글 keyword 검색의 대소문자 처리
- `AdminTemplateContractTest`: `admin-dashboard`, `admin-users`, `admin-boards`, `admin-board-action-menu`, `admin-sidebar`
- `./gradlew.bat test`: 전체 기준선 확인

## 의존

- 내부: `_core/interceptor`, `../board/BoardRepository.java`, `../board/BoardLikeRepository.java`, `../board/BoardCategory.java`, `../user/UserRepository.java`
- 템플릿: `src/main/resources/templates/partials/admin-sidebar.mustache`, `src/main/resources/templates/partials/admin-page-scripts.mustache`, `src/main/resources/templates/pages/admin-dashboard.mustache`, `src/main/resources/templates/pages/admin-users.mustache`, `src/main/resources/templates/pages/admin-boards.mustache`
- 정적 자산: `src/main/resources/static/css/admin.css`
