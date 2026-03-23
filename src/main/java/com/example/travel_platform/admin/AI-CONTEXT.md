<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 대시보드, 사용자 관리, 게시글 관리 화면을 조립하는 SSR 도메인이다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `AdminController.java` | `/admin`, `/admin/users`, `/admin/boards`, `/admin/boards/{boardId}/delete` 요청을 받아 관리자 화면을 렌더링하거나 redirect query를 유지한다. |
| `AdminService.java` | 관리자 화면에 필요한 조회 결과를 조합하고 페이지 DTO를 완성해 반환한다. |
| `AdminQueryRepository.java` | 관리자 화면 전용 사용자/게시글/대시보드 row 조회를 담당한다. |
| `AdminResponse.java` | 관리자 화면 DTO를 정의한다. |

## 현재 구조 기준

- `admin`은 자체 엔티티를 소유하는 도메인이 아니라 `user`, `board`를 엮어 관리자 화면을 조립하는 도메인이다.
- 조회는 `AdminQueryRepository`에 모으고, 상태 변경은 기존 도메인 repository를 사용한다.
  - 사용자 활성/비활성 변경: `UserRepository`
  - 게시글 삭제: `BoardRepository`, `BoardLikeRepository`
- `/admin`은 `model` 루트 계약을 사용한다.
- `/admin/users`, `/admin/boards`는 `model + models` 계약을 사용한다.
- `AdminController`는 DTO를 후처리하지 않고, `AdminService`가 완성한 DTO를 그대로 전달한다.

## DTO 조립 규칙

- `AdminResponse`의 페이지 DTO는 생성 시점에 메뉴 active class를 포함한다.
- 사용자 목록 페이지는 탭 href까지 `createUserListPage(...)`에서 완성한다.
- 대시보드/게시글 관리도 컨트롤러의 `apply...` helper 없이 DTO 생성 단계에서 화면 상태를 확정한다.

## 테스트

- `AdminControllerTest`
- `AdminServiceDashboardTest`
- `AdminServiceTest`
- `AdminQueryRepositoryTest`
- `AdminTemplateContractTest`

검증은 `./gradlew.bat test --tests com.example.travel_platform.admin.AdminControllerTest --tests com.example.travel_platform.admin.AdminServiceDashboardTest` 와 `./gradlew.bat test` 기준으로 맞춘다.
