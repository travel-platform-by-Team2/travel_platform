<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

로그인 사용자 전용 마이페이지 SSR 화면, 비밀번호 변경/회원 탈퇴 흐름, 예약/여행 요약 카드, 예약 상세 placeholder 화면을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| MypageController.java | `/mypage`, `/mypage/password`, `/mypage/withdraw`, `/mypage/bookings/{bookingId}` 요청을 처리한다. |
| MypageRequest.java | 비밀번호 변경, 회원 탈퇴 입력 DTO를 정의한다. |
| MypageResponse.java | 마이페이지 화면 DTO, 카드 변환 로직, 예약 상세 placeholder DTO를 정의한다. |
| MypageService.java | 프로필 조회, 다가오는 예약/여행 계획 카드 조합, 비밀번호 변경을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 이 패키지는 `UserRepository`, `UserService`, `BookingRepository`, `TripRepository`를 재사용해 프로필과 예약/여행 요약, 탈퇴 흐름을 연결한다.
- `/mypage`는 로그인 사용자 전용 SSR 페이지이며 `_core/filter/LoginFilter`와 함께 봐야 한다.
- 비밀번호 변경 실패는 같은 요청에서 `pages/mypage`를 다시 렌더링해 모달 에러를 보여주고, 성공은 redirect 후 토스트형 1회성 메시지로 처리한다.
- 회원 탈퇴 실패도 같은 요청에서 `pages/mypage`를 다시 렌더링해 모달 에러를 보여주고, 성공은 세션 종료 후 `/login-form` 으로 redirect 한다.
- `MypageController`는 `renderMainPage(...)` helper로 메인 화면 공통 모델 조립을 모은다.
- 예약 섹션은 본인 예약 중 `checkIn >= 오늘` 조건을 만족하는 데이터만 체크인 오름차순으로 최대 2건 노출한다.
- 여행 계획 섹션은 본인 계획 중 `startDate >= 오늘` 조건을 만족하는 데이터만 시작일 오름차순으로 최대 2건 노출한다.
- 여행 계획 카드의 상세 링크는 기존 SSR 라우트 `/trip/detail?id={planId}` 계약을 따른다.
- `MypageResponse.ProfileDTO.withdrawAllowed` 로 관리자 계정의 탈퇴 버튼 노출을 제어한다.
- `MypageResponse.PageDTO`, `ProfileDTO`, `BookingCardDTO`, `PlanCardDTO`, `BookingDetailPageDTO`가 화면 DTO 조립 책임을 나눠 가진다.
- `mypage.mustache`, `booking-detail.mustache`는 아직 더미/placeholder가 섞여 있으므로 임의로 구조를 바꾸지 않는다.
- `/mypage/bookings/{bookingId}`는 현재 placeholder 상세 페이지이며, 이번 단계에서는 bookingId만 모델에 연결한다.

## 테스트

- `/mypage`가 로그인 사용자 기준으로 정상 렌더링되는지 확인한다.
- 다가오는 예약이 0건, 1건, 2건 이상일 때 카드 섹션이 올바르게 렌더링되는지 확인한다.
- 비밀번호 변경 실패 시 모달 에러가 뜨고, 성공 시 토스트 메시지가 보이는지 확인한다.
- 일반 사용자는 탈퇴 버튼이 보이고, 관리자는 보이지 않는지 확인한다.
- 탈퇴 실패 시 탈퇴 모달 에러가 뜨고, 성공 시 세션 종료 후 `/login-form` 으로 이동하는지 확인한다.
- 조건에 맞는 여행 계획이 0건, 1건, 2건 이상일 때 카드 섹션이 올바르게 렌더링되는지 확인한다.
- `/mypage/bookings/{bookingId}`가 placeholder 화면으로 정상 진입하고 bookingId를 표시하는지 확인한다.
- `./gradlew.bat test`로 자동 테스트를 확인한다.
- 현재 기준으로 자동 테스트는 완료됐고, 최신 예약 카드/상세 라우트 및 탈퇴 모달 상호작용에 대한 브라우저 수동 검증은 추가 확인이 필요하다.

## 의존성

- 파일: `../user/UserRepository.java`, `../user/UserService.java`, `../booking/BookingRepository.java`, `../trip/TripRepository.java`, `src/main/resources/templates/pages/mypage.mustache`, `src/main/resources/templates/pages/booking-detail.mustache`
- 기술: `Spring MVC`
