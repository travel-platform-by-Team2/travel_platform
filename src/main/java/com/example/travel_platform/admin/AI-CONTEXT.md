<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 페이지의 SSR 진입 흐름과 대시보드 요약 데이터, 사용자/게시글 관리 화면 조립을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| AdminController.java | `/admin`, `/admin/users`, `/admin/boards`, `/admin/boards/{boardId}/delete` 요청을 받아 관리자 Mustache 페이지를 렌더링하거나 삭제 redirect를 처리한다. |
| AdminService.java | 관리자 대시보드, 사용자 관리, 게시글 관리 화면에 필요한 row 조회 결과를 DTO로 조립한다. |
| AdminUserQueryRepository.java | 관리자 화면에서 재사용하는 사용자 목록/검색/최근 사용자 JPQL constructor query를 담당한다. |
| AdminBoardQueryRepository.java | 관리자 화면에서 재사용하는 게시글 목록/검색/최근 게시글/집계 JPQL constructor query를 담당한다. |
| AdminResponse.java | 관리자 화면 DTO를 정의한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `admin`은 자체 도메인 엔티티를 소유하는 패키지가 아니라 `user`, `board`를 읽어 관리자 화면을 조립하는 조회 중심 패키지로 본다.
- 읽기 전용 경로는 `AdminUserQueryRepository`, `AdminBoardQueryRepository`의 JPQL row 조회를 우선 사용한다.
- 액션 경로는 기존 도메인 repository를 유지한다.
  - 사용자 상태 변경: `UserRepository`
  - 게시글 삭제: `BoardRepository`, `BoardLikeRepository`
- `AdminController`는 `renderDashboardPage(...)`, `renderUsersPage(...)`, `renderBoardsPage(...)` helper로 화면 진입을 분리한다.
- `/admin` 대시보드는 `model` 단건 루트 계약을 사용한다.
- `/admin/users`, `/admin/boards`는 `model + models` 루트 계약을 사용한다.
- `AdminResponse` 정적 팩토리 메서드는 `createDashboardView`, `createUserListView`, `createBoardListView`, `createAdminUser`처럼 역할이 드러나는 이름을 사용한다.
- `admin` 정규화는 schema 변경이 아니라 조회 경로와 화면 조립 책임을 분리하는 방향으로 본다.

## 테스트

- `AdminControllerTest`에서 관리자 페이지 진입과 게시글 삭제 redirect 흐름을 직접 호출 방식으로 확인한다.
- `AdminServiceDashboardTest`에서 대시보드, 사용자 목록, 게시글 목록 조립 결과를 실제 시드 기준으로 확인한다.
- `AdminServiceTest`에서 게시글 삭제의 `401/403/404/정상` 흐름과 게시글 목록 조립을 mock 기반으로 확인한다.
- `AdminTemplateContractTest`에서 `admin-dashboard`, `admin-users`, `admin-boards`가 `model/models` 계약을 따르는지 확인한다.
- `./gradlew.bat test`로 전체 회귀를 확인한다.

## 의존성

- 내부: `_core/interceptor`, `../board/BoardRepository.java`, `../board/BoardLikeRepository.java`, `../board/BoardCategory.java`, `../user/UserRepository.java`, `src/main/resources/templates/partials/admin-sidebar.mustache`, `src/main/resources/templates/partials/admin-page-scripts.mustache`, `src/main/resources/templates/pages/admin-dashboard.mustache`, `src/main/resources/templates/pages/admin-users.mustache`, `src/main/resources/templates/pages/admin-boards.mustache`, `src/main/resources/static/css/admin.css`
- 외부: `Spring MVC`, `Spring Data JPA`
