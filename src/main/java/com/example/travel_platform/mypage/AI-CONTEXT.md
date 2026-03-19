<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

로그인 사용자 전용 마이페이지 SSR 화면, 비밀번호 변경 흐름, 여행 계획 요약 카드, 예약 상세 placeholder 화면을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| MypageController.java | `/mypage`, `/mypage/password`, `/mypage/booking` 요청을 처리한다. |
| MypageRequest.java | 비밀번호 변경 입력 DTO를 정의한다. |
| MypageResponse.java | 마이페이지 화면 DTO와 카드 변환 로직을 정의한다. |
| MypageService.java | 프로필 조회, 다가오는 여행 계획 카드 조합, 비밀번호 변경을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 이 패키지는 `UserRepository`와 기존 `TripRepository`를 재사용해 프로필과 여행 계획 요약을 조회한다.
- `/mypage`는 로그인 사용자 전용 SSR 페이지이며 `_core/filter/LoginFilter`와 함께 봐야 한다.
- 비밀번호 변경 실패는 같은 요청에서 `pages/mypage`를 다시 렌더링해 모달 에러를 보여주고, 성공은 redirect 후 토스트형 1회성 메시지로 처리한다.
- `MypageController`는 `renderMainPage(...)` helper로 메인 화면 공통 모델 조립을 모은다.
- 여행 계획 섹션은 본인 계획 중 `startDate >= 오늘` 조건을 만족하는 데이터만 시작일 오름차순으로 최대 2건 노출한다.
- `MypageResponse.PageDTO`, `ProfileDTO`, `PlanCardDTO`가 화면 DTO 조립 책임을 나눠 가진다.
- `mypage.mustache`, `booking-detail.mustache`는 아직 더미/placeholder가 섞여 있으므로 임의로 구조를 바꾸지 않는다.
- `/mypage/booking`은 bookingId 없는 정적 상세 페이지이므로 실제 예약 상세 연동 전에는 URL 계약부터 먼저 정리해야 한다.

## 테스트

- `/mypage`가 로그인 사용자 기준으로 정상 렌더링되는지 확인한다.
- 비밀번호 변경 실패 시 모달 에러가 뜨고, 성공 시 토스트 메시지가 보이는지 확인한다.
- 조건에 맞는 여행 계획이 0건, 1건, 2건 이상일 때 카드 섹션이 올바르게 렌더링되는지 확인한다.
- `./gradlew.bat test`로 자동 테스트를 확인한다.
- 현재 기준으로 자동 테스트와 브라우저 수동 검증이 모두 완료됐다.

## 의존성

- 파일: `../user/UserRepository.java`, `../trip/TripRepository.java`, `src/main/resources/templates/pages/mypage.mustache`, `src/main/resources/templates/pages/booking-detail.mustache`
- 기술: `Spring MVC`
