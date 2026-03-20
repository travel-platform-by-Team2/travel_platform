<!-- Parent: ../AI-CONTEXT.md -->

리팩토링 완료

# user

## 목적

회원가입, 로그인, 세션 사용자/권한 처리, 마이페이지 탈퇴를 포함한 사용자 계정 핵심 흐름과 메인 진입 화면을 담당한다.

## 주요 파일

| 파일명                  | 설명                                                                                    |
| ----------------------- | --------------------------------------------------------------------------------------- |
| User.java               | 사용자 엔티티다.                                                                        |
| SessionUser.java        | 세션에 저장하는 사용자 전용 DTO다.                                                      |
| SessionUsers.java       | 세션 사용자 조회/저장 helper다.                                                         |
| UserSessionChecker.java | 세션 사용자 기준으로 최신 DB 상태를 확인해 비활성 차단 여부를 판단하는 전용 컴포넌트다. |
| UserController.java     | 로그인, 로그아웃, 회원가입, 메인 화면 요청을 처리한다.                                  |
| UserRepository.java     | 사용자 저장/조회/삭제 저장소다.                                                         |
| UserRequest.java        | 사용자 입력 DTO를 정의한다.                                                             |
| UserResponse.java       | 사용자 응답 DTO를 정의한다.                                                             |
| UserService.java        | 회원가입, 로그인, 회원 탈퇴 비즈니스 로직을 처리한다.                                   |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 세션 키는 `sessionUser`를 사용하고, 현재 세션에는 `User` 엔티티가 아니라 `SessionUser` DTO를 저장한다.
- 세션 조회/저장은 가능하면 `SessionUsers` helper를 우선 사용하고, 직접 `session.getAttribute("sessionUser")`를 퍼뜨리지 않는다.
- `User.role`은 `_core/interceptor/AdminInterceptor`의 관리자 판별과 공용 헤더의 관리자 대시보드 버튼 노출 조건에 사용되므로, 권한 체계를 바꾸면 `/admin`, `/admin/*` 접근 규칙과 헤더 UI를 함께 조정해야 한다.
- 일반 사용자 비활성 차단은 `UserService.login(...)`과 `UserSessionChecker` + `_core/interceptor` 조합으로 처리한다. 로그인 시도는 `현재 로그인할 수 없는 계정입니다.`로 막고, 로그인 중 비활성 감지는 `계정 상태가 변경되어 다시 로그인해 주세요.` 문구로 강제 로그아웃한다.
- 로그인/회원가입 폼 필드 이름은 DTO와 템플릿이 맞물려 있으므로 이름 변경을 한쪽만 하지 않는다.
- `UserController`는 `/`, `/login-form`, `/join-form`, `/login`, `/join`, `/logout`를 담당하므로 인증 진입 경로를 바꿀 때 redirect 흐름도 같이 확인한다.
  <<<<<<< HEAD
- # `SessionUser` 계약은 `mypage`, `_core/interceptor`, `board`, `trip`, `booking`, `calendar`의 세션 참조 코드와 연결되어 있으므로 변경 시 직접 영향 범위를 같이 수정해야 한다.
- `SessionUser` 계약은 `mypage`, `_core/filter`, `board`, `trip`, `booking`, `calendar`의 세션 참조 코드와 연결되어 있으므로 변경 시 직접 영향 범위를 같이 수정해야 한다.
  > > > > > > > dev
- 회원 탈퇴는 `active` 변경이 아니라 실제 `user_tb` 삭제이며, 관리자 계정은 서비스 레벨에서 탈퇴를 막는다.
- 탈퇴 로직은 FK 제약 때문에 `board_like`, `reply`, `board`, `calendar_event`, `booking`, `trip_place`, `trip_plan`, `user` 순의 정리 흐름을 함께 봐야 한다.
- `UserService.withdrawAccount(...)`는 현재 비밀번호 확인 후 관련 저장소를 호출해 연관 데이터를 지운 다음 `UserRepository.delete(...)`를 수행한다.

## 테스트

- 로그인, 로그아웃, 회원가입과 세션 유지 흐름을 확인한다.
- 비활성 일반 사용자는 로그인할 수 없고, 로그인 중 비활성 처리되면 다음 요청에서 세션이 종료되는지 확인한다.
- 로그인 후 세션에 `SessionUser` DTO가 저장되고 헤더가 정상 노출되는지 확인한다.
- `/login-form` 접근, `/join` 검증 실패 시 `pages/signup` 재렌더링, `/logout` 후 세션 제거를 함께 점검한다.
- 관리자 계정이 도입된 상태라면 로그인 후 `sessionUser.role` 값, 헤더의 관리자 버튼 노출, `/admin` 접근, 관리자 필터 동작을 함께 확인한다.
- `mypage` 비밀번호 변경 후에도 세션/필터/헤더 흐름이 정상인지 함께 확인한다.
- 회원 탈퇴 시 일반 사용자 삭제, 관리자 탈퇴 차단, 비밀번호 불일치 실패, 세션 종료를 함께 확인한다.

## 의존성

- 내부: `_core/interceptor`, `mypage`, `admin`, `board`, `trip`, `booking`, `calendar`, `src/main/resources/templates/pages/main-index.mustache`, `src/main/resources/templates/pages/login.mustache`, `src/main/resources/templates/pages/signup.mustache`
- 외부: `Spring MVC`, `JPA/Hibernate`
