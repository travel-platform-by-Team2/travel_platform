# 2026-03-03

## 작업 내용용

### 로그인 기능 수정

- **로그인 방식 변경**:
  - 기존의 `username` 기반 로그인에서 `email` 기반 로그인으로 변경하였습니다.
  - 사용자는 이제 이메일과 비밀번호를 사용하여 로그인합니다.

- **로그인 성공 시 리다이렉션**:
  - 로그인에 성공하면 메인 페이지 (`/main-index`)로 이동하도록 수정했습니다.

- **로그인 실패 시 오류 처리**:
  - 잘못된 이메일이나 비밀번호를 입력하면 경고창(alert)을 통해 사용자에게 실패 사실을 알립니다.

### 주요 수정 파일

- **`src/main/resources/templates/pages/login.mustache`**:
  - 로그인 폼의 이메일, 비밀번호 input에 `name` 속성을 추가하여 서버에 정상적으로 값이 전달되도록 수정했습니다.
  - 로그인 버튼의 `type`을 `submit`으로 변경하여 폼 제출이 가능하도록 했습니다.
  - 불필요한 기본 오류 메시지를 숨김 처리했습니다.

- **`src/main/java/com/example/travel_platform/user/UserRequest.java`**:
  - `LoginDTO`가 `username` 대신 `email`을 받도록 수정했습니다.

- **`src/main/java/com/example/travel_platform/user/UserRepository.java`**:
  - 이메일로 사용자를 조회할 수 있는 `findByEmail` 메소드를 추가했습니다.

- **`src/main/java/com/example/travel_platform/user/UserService.java`**:
  - `findByEmail` 메소드를 사용하도록 `로그인` 서비스 로직을 업데이트했습니다.

- **`src/main/java/com/example/travel_platform/user/UserController.java`**:
  - `login` 메소드가 이메일을 사용해 인증을 처리하고, 성공 시 `/main-index`로 리다이렉트하도록 수정했습니다.
  - 보안 상 불필요한 쿠키 생성 로직을 제거했습니다.

## 작업완료건

20260305

1. 로그인 성공: UserController에서 세션 저장 후 메인 페이지(/)로 리다이렉트.
2. 로그인 실패: UserService에서 이메일/비밀번호 확인 후 예외 발생(Exception400/401).
3. 회원가입 성공: UserController에서 가입 후 로그인 폼(/login-form)으로 리다이렉트.
4. 회원가입 실패: UserService에서 중복 체크 후 예외 발생(Exception400).

추가보완

- [ ] 현재는 서비스 이용약관을 클릭하지 않아도 회원가입이 가능한 상태 이후 체크한 경우만 회원가입 가능하도록 구현
- [ ] 하단 “회원가입 하기”로 가능하게 구현
- [ ] jiyun@naver.com처럼 새로운 정보로 회원가입은 되지만 로그인은 되지 않는 상황 → 더미데이터 초기화의 문제인것인지 회원가입 후 로그인이 진행되지 않는 것인지 확인필요
