<!-- Parent: ../AI-CONTEXT.md -->

# admin

## 목적

관리자 페이지의 SSR 진입 라우트와 화면 연결을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| AdminController.java | `/admin` 요청을 받아 관리자 대시보드 Mustache 페이지를 렌더링한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `admin` 패키지는 새 비즈니스 엔티티 도메인이 아니라 관리자 화면 진입 패키지로 유지한다.
- 관리자 권한 검사는 컨트롤러 안에서 중복 구현하지 말고 `_core/filter/AdminFilter`의 `/admin`, `/admin/*` 보호를 재사용한다.
- 대시보드 외 관리자 하위 페이지를 추가할 때도 같은 패키지의 SSR 컨트롤러에서 라우트를 확장하고, 실제 데이터 조회는 기존 도메인 서비스를 재사용하거나 전용 조회 DTO로 조합한다.

## 테스트

- 관리자 로그인 상태에서 `/admin`이 `pages/admin-dashboard`를 렌더링하는지 확인한다.
- 비관리자 또는 비로그인 상태에서 `/admin` 접근 시 관리자 필터가 차단하는지 함께 점검한다.

## 의존성

- 내부: `_core/filter`, `src/main/resources/templates/pages/admin-dashboard.mustache`
- 외부: `Spring MVC`
