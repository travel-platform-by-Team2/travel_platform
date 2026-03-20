<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 페이지의 SSR 진입 라우트, 대시보드 요약 데이터, 유저/게시글 관리 화면 연결을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| AdminController.java | `/admin`, `/admin/users`, `/admin/lodgings`, `/admin/boards` 요청을 받아 관리자 Mustache 페이지를 렌더링하고 사이드바 활성 상태를 세팅한다. |
| AdminService.java | 관리자 대시보드, 유저 관리, 게시글 관리 3개 페이지 기준으로 서비스 메서드를 나눠 조회와 화면 DTO 조립을 담당한다. |
| AdminRepository.java | 관리자 화면에서 재사용하는 사용자 조회와 집계 쿼리를 담당한다. |
| AdminResponse.java | 대시보드, 유저 관리, 게시글 관리 화면 DTO를 정의한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `admin` 패키지는 새 비즈니스 엔티티 도메인이 아니라 관리자 화면 진입과 관리자용 조회 조립 패키지로 유지한다.
- 관리자 권한 검사는 컨트롤러 안에서 중복 구현하지 말고 `_core/interceptor/AdminInterceptor`의 `/admin`, `/admin/*` 보호를 재사용한다.
- `admin`은 현재 페이지 3장(`/admin`, `/admin/users`, `/admin/boards`) 기준으로 유지하며, 파일을 과하게 쪼개기보다 `AdminService` 내부 메서드 책임을 페이지 단위로 읽기 쉽게 정리하는 방향을 우선한다.
- `/admin` 대시보드는 현재 실데이터 기반 요약 화면이며, `AdminService.getDashboardPage()`가 유저/게시글 요약 카드, 그래프 2개, 최근 유저/최근 게시글 3건을 조립한다.
- `/admin/users`는 `AdminService.getUsersPage(...)`가 목록, 카운트, 탭 상태를 한 번에 조립하고, 컨트롤러는 이를 기존 Mustache 모델 키로 풀어 넣는다.
- `/admin/boards`는 `AdminService.getBoardsPage(...)`가 목록, 페이지네이션, 카테고리 선택 상태를 조립한다.
- 대시보드 데이터는 현재 코드베이스에서 바로 구할 수 있는 유저/게시글 범위만 사용하며, 차트 라이브러리 없이 Mustache + CSS 기반 막대/비율형 시각화로 표현한다.
- 공통 왼쪽 패널은 현재 `대시보드`, `유저 관리`, `게시글 관리` 3개 메뉴만 노출하며, `숙소 관리` 경로는 라우트는 남아 있어도 사이드바 메뉴에서는 제외된 상태다.
- `admin-users`, `admin-boards` 페이지는 기존 화면 구조를 유지하고, 대시보드 작업 때문에 템플릿을 건드리지 않는 것을 기본 원칙으로 본다.
- 유저/게시글 관리 페이지의 액션 패널은 더미 UI 인터랙션이며 실제 처리 로직과 분리해 유지한다.

## 테스트

- 관리자 로그인 상태에서 `/admin`, `/admin/users`, `/admin/lodgings`, `/admin/boards`가 각각 올바른 `pages/admin-*` 템플릿을 렌더링하는지 확인한다.
- `/admin` 대시보드에서 요약 카드, 사용자 상태 그래프, 게시글 카테고리 그래프, 최근 유저/최근 게시글 3건이 실데이터로 렌더링되는지 확인한다.
- 사이드바 활성 메뉴, 대시보드 `전체보기` 링크, 액션 패널 UI 동작을 함께 점검한다.
- 비관리자 또는 비로그인 상태에서 `/admin*` 접근 시 관리자 필터가 차단하는지 함께 점검한다.
- `./gradlew.bat test`로 자동 테스트를 확인한다.

## 의존성

- 내부: `_core/interceptor`, `../board/BoardRepository.java`, `../user/User.java`, `src/main/resources/templates/partials/admin-sidebar.mustache`, `src/main/resources/templates/partials/admin-page-scripts.mustache`, `src/main/resources/templates/pages/admin-dashboard.mustache`, `src/main/resources/templates/pages/admin-users.mustache`, `src/main/resources/templates/pages/admin-lodgings.mustache`, `src/main/resources/templates/pages/admin-boards.mustache`, `src/main/resources/static/css/admin.css`
- 외부: `Spring MVC`, `Spring Data JPA`
