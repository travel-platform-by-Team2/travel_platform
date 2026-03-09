## 1. 작업 목표 및 작업 내용
### 상세 작업 방법
1. **UserController.java 수정**
   - `logout` 메서드의 리다이렉션 경로를 `/login-form`에서 `/`(메인 페이지)로 변경한다.
2. **header-logged-in.mustache 수정**
   - 로그아웃 버튼의 `href` 속성을 `#`에서 `/logout`으로 변경한다.

### 수행한 작업
- [x] **로그인 페이지(login.mustache) 수정**: '회원가입 하기' 링크를 `/join-form`으로 연결
- [x] **회원가입 페이지(signup.mustache) 수정**: '로그인' 링크를 `/login-form`으로 연결
- [x] **UserController.java 수정**: 로그아웃 시 `/` (메인 페이지)로 리다이렉트되도록 변경
- [x] **상단 헤더(header-logged-in.mustache) 수정**: 로그아웃 버튼에 `/logout` 링크 연결
