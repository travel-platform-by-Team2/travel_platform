<!-- Parent: ../AI-CONTEXT.md -->

# user

## 목적

회원가입, 로그인, 세션 사용자 처리와 메인 진입 화면을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| User.java | 사용자 엔티티다. |
| UserController.java | 로그인, 로그아웃, 회원가입, 메인 화면 요청을 처리한다. |
| UserRepository.java | 사용자 저장/조회 저장소다. |
| UserRequest.java | 사용자 입력 DTO를 정의한다. |
| UserResponse.java | 사용자 응답 DTO를 정의한다. |
| UserService.java | 회원 비즈니스 로직을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 세션 키는 `sessionUser`를 사용하므로 인증 흐름을 바꿀 때 필터와 헤더 UI를 같이 확인한다.
- 로그인/회원가입 폼 필드 이름은 DTO와 템플릿이 맞물려 있으므로 이름 변경을 한쪽만 하지 않는다.
- `UserController`는 `/`, `/login-form`, `/join-form`, `/login`, `/join`, `/logout`를 담당하므로 인증 진입 경로를 바꿀 때 redirect 흐름도 같이 확인한다.

## 테스트

- 로그인, 로그아웃, 회원가입과 세션 유지 흐름을 확인한다.
- `/login-form` 접근, `/join` 검증 실패 시 `pages/signup` 재렌더링, `/logout` 후 세션 제거를 함께 점검한다.

## 의존성

- 내부: `_core/filter`, `src/main/resources/templates/pages/main-index.mustache`, `src/main/resources/templates/pages/login.mustache`, `src/main/resources/templates/pages/signup.mustache`
- 외부: `Spring MVC`, `JPA/Hibernate`
