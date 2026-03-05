# 2026-03-03

## 작업 내용

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
