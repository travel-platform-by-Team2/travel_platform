<!-- Parent: ../AI-CONTEXT.md -->

# user

## 목적

회원가입, 로그인, SNS 로그인, 세션 계약, 비활성 사용자 차단, 회원 탈퇴와 로그인 관련 화면 진입을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `User.java` | 사용자 엔티티다. |
| `SessionUser.java` | 세션에 저장하는 사용자 DTO다. |
| `SessionUsers.java` | `sessionUser` 조회, 저장, 필수 로그인 검사를 담당하는 helper다. |
| `UserQueryRepository.java` | 로그인, SNS 로그인, 사용자명 중복 검사 같은 조회 전용 query 저장소다. |
| `UserRepository.java` | 사용자 저장, 단건 조회, 삭제를 담당하는 저장소다. |
| `UserSessionChecker.java` | 세션 사용자 기준으로 최신 DB 상태를 확인하고 비활성 사용자 차단 여부를 판단한다. |
| `UserController.java` | 메인, 로그인 화면, 회원가입 화면 진입과 로그인, 로그아웃, SNS callback 흐름을 담당한다. |
| `UserRequest.java` | 회원가입, 로그인, SNS callback 입력 DTO를 정의한다. |
| `UserResponse.java` | 로그인 화면 모델 DTO를 정의한다. |
| `UserService.java` | 회원가입, 로그인, SNS 로그인, 회원 탈퇴 비즈니스 로직을 담당한다. |

## 작업 기준

- 세션 키는 항상 `sessionUser`를 사용한다.
- 세션 조회/저장은 `SessionUsers` helper를 우선 사용한다.
- 세션에는 `User` 엔티티가 아니라 `SessionUser` DTO를 저장한다.
- `SessionUsers.getOrNull(...)`은 legacy `User` 세션을 읽으면 `SessionUser`로 마이그레이션한다.
- `User.role`은 `UserRole` enum, `User.provider`는 `UserAuthProvider` enum으로 저장하고 외부 계약은 문자열 코드 getter로 유지한다.
- `SessionUser`는 JPA enum을 직접 들고 가지 않고, 세션 독립성을 위해 문자열 코드 스냅샷을 유지한다.
- `UserController`는 화면 진입, 세션 처리, redirect 흐름만 담당한다.
- 로그인 화면 `/login-form`은 `model` 루트에 `UserResponse.LoginPageModelDTO`를 담아 `pages/login`을 렌더링한다.
- `main-index`, `signup`은 현재 정적 화면이므로 추가 모델 없이 렌더링한다.
- `UserService`는 세션을 직접 만지지 않고 비즈니스 로직만 담당한다.
- `UserService.loginWithSns(...)`는 기존 `snsLogin(...)`을 대체하는 명시적 메서드고, `snsLogin(...)`은 호환용 위임 메서드다.
- `UserService.loginWithSns(...)`는 `provider` 문자열을 `UserAuthProvider`로 해석한 뒤 조회/생성 흐름을 수행한다.
- `UserService`는 조회 전용 로직에 `UserQueryRepository`를 사용하고, 저장/삭제는 `UserRepository`를 사용한다.
- 일반 사용자가 비활성 상태면 로그인과 인터셉터 모두 차단된다.
- 관리자 계정은 `UserSessionChecker`에서 비활성 차단 대상에서 제외된다.
- 회원 탈퇴는 관련 게시글, 댓글, 좋아요, 여행, 예약, 일정 데이터를 먼저 정리한 뒤 `UserRepository.delete(...)`를 수행한다.

## 테스트

- `UserControllerTest`는 로그인 화면 `model` 계약과 SNS callback 세션 갱신 흐름을 검증한다.
- `UserCodeTest`는 `UserRole`, `UserAuthProvider`, `SessionUser.fromUserEntity(...)` 기준을 검증한다.
- `UserResponseTest`는 로그인 화면 모델 DTO 생성을 검증한다.
- `UserServiceTest`, `UserServiceLoginAndFilterTest`, `UserServiceWithdrawalTest`는 로그인, SNS 로그인, 비활성 차단, 탈퇴 흐름을 검증한다.
- `SessionUsersTest`는 legacy 세션 마이그레이션과 로그인 필수 예외를 검증한다.
- `UserTemplateContractTest`는 `main-index`, `login`, `signup` 템플릿 계약을 검증한다.

## 의존

- `_core/interceptor`
- `mypage`
- `admin`
- `board`
- `trip`
- `booking`
- `calendar`
- `src/main/resources/templates/pages/main-index.mustache`
- `src/main/resources/templates/pages/login.mustache`
- `src/main/resources/templates/pages/signup.mustache`
