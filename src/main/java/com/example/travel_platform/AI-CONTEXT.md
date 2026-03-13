# travel_platform

## 목적

여행 플랫폼의 메인 애플리케이션 패키지로, 공통 기능과 도메인별 기능이 이 아래로 분리된다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| TravelPlatformApplication.java | Spring Boot 애플리케이션 진입점이다. |

## 하위 디렉토리

- `_core/` - 로그인/관리자 접근 제어, 예외 처리, 유틸리티를 둔다.
- `board/` - 게시판 기능을 둔다.
- `booking/` - 예약 및 지도 상세 기능을 둔다.
- `calendar/` - 일정 캘린더 기능을 둔다.
- `chatbot/` - 챗봇 기능을 둔다.
- `mypage/` - 마이페이지 조합 화면 기능을 둔다.
- `trip/` - 여행 계획 기능을 둔다.
- `user/` - 회원 기능을 둔다.

## AI 작업 지침

- 페이지용 `@Controller`와 JSON용 `@RestController`를 분리하는 현재 구조를 유지한다.
- 공통 예외, 필터, 응답 포맷은 `_core`에 두고 도메인 패키지에 중복시키지 않는다.
- `/admin/*` 접근 제어는 컨트롤러 안에서 중복 구현하지 말고 `_core/filter`의 관리자 필터를 기준으로 맞춘다.
- 일부 API와 서비스에는 `userId=1` 고정이나 TODO 구현이 남아 있으므로 인증/영속성 변경 시 영향 범위를 함께 확인한다.

## 테스트

- PowerShell에서는 `./gradlew.bat test`로 전체 회귀 테스트를 실행한다.

## 의존성

- 내부: `_core`, `board`, `booking`, `calendar`, `chatbot`, `mypage`, `trip`, `user`
- 외부: `Spring Boot`, `Spring MVC`, `Spring JDBC`, `JPA/Hibernate`, `Mustache`, `H2`, `MySQL`, `Gson`, `Jsoup`, `OpenAI Responses API`
