<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

로그인 사용자 전용 마이페이지 SSR 화면, 비밀번호 변경/회원 탈퇴 흐름, 예약/여행 요약 카드, 예약 상세 placeholder 화면을 담당한다.

## 주요 파일

| 파일명                | 설명                                                                                               |
| --------------------- | -------------------------------------------------------------------------------------------------- |
| MypageController.java | `/mypage`, `/mypage/password`, `/mypage/withdraw`, `/mypage/bookings/{bookingId}` 요청을 처리한다. |
| MypageRequest.java    | 비밀번호 변경, 회원 탈퇴 입력 DTO를 정의한다.                                                      |
| MypageResponse.java   | 마이페이지 화면 DTO, 카드 변환 로직, 예약 상세 placeholder DTO를 정의한다.                         |
| MypageService.java    | 프로필 조회, 다가오는 예약/여행 계획 카드 조합, 비밀번호 변경을 처리한다.                          |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 이 패키지는 `UserRepository`, `UserService`, `BookingRepository`, `TripRepository`를 재사용해 프로필과 예약/여행 요약, 탈퇴 흐름을 연결한다.
- `/mypage`는 로그인 사용자 전용 SSR 페이지이며 `_core/interceptor/LoginInterceptor`와 함께 봐야 한다.
- `MypageController`는 `showMainPage`, `changePassword`, `withdrawAccount`, `showBookingDetailPage`를 모두 helper 기반으로 정리했고, 화면 렌더링은 `page` 루트 DTO 하나로 전달한다.
- 비밀번호 변경 실패는 같은 요청에서 `pages/mypage`를 다시 렌더링해 모달 에러를 보여주고, 성공은 redirect 후 토스트형 1회성 메시지로 처리한다.
- 회원 탈퇴 실패도 같은 요청에서 `pages/mypage`를 다시 렌더링해 모달 에러를 보여주고, 성공은 세션 종료 후 `/login-form` 으로 redirect 한다.
- `MypageController`는 `renderMainPage(...)`, `renderPasswordFailure(...)`, `renderWithdrawFailure(...)`, `renderBookingDetailPage(...)` helper로 메인 화면과 placeholder 상세 진입을 정리했다.
- 예약 섹션은 본인 예약 중 `checkIn >= 오늘` 조건을 만족하는 데이터만 체크인 오름차순으로 최대 2건 노출한다.
- 여행 계획 섹션은 본인 계획 중 `startDate >= 오늘` 조건을 만족하는 데이터만 시작일 오름차순으로 최대 2건 노출한다.
- 여행 계획 카드의 상세 링크는 기존 SSR 라우트 `/trip/detail?id={planId}` 계약을 따른다.
- `MypageResponse.ProfileDTO.withdrawAllowed` 로 관리자 계정의 탈퇴 버튼 노출을 제어한다.
- `MypageResponse.PageDTO`는 프로필/카드 리스트 외에도 `passwordError`, `passwordModalOpen`, `withdrawError`, `withdrawModalOpen`, `passwordSuccessMessage`를 함께 가진다.
- `mypage.mustache`는 `page` 루트만 읽고, root model 속성을 직접 기대하지 않는다.
- `/mypage/bookings/{bookingId}`는 현재 placeholder 상세 페이지이며, 컨트롤러도 로그인 세션을 명시적으로 요구한다.
- `MypageResponse.BookingDetailPageDTO`는 `bookingId`, `backLink`, `placeholderNotice`만 가진 placeholder 전용 DTO다.
- `booking-detail.mustache`는 예약 실데이터가 아니라 `page.bookingId`, `page.backLink`, `page.placeholderNotice`만 읽는다.
- 예약 상세 실데이터 조회나 소유자 검증은 이번 리팩토링 범위가 아니므로 임의로 구현하지 않는다.

## 테스트

- `MypageControllerTest`에서 메인 화면, 비밀번호 변경 실패/성공, 탈퇴 실패/성공, 예약 상세 placeholder, 세션 없는 접근 차단을 직접 호출 방식으로 확인한다.
- `MypageServiceTest`에서 다가오는 예약과 여행 계획 카드, 관리자 탈퇴 버튼 노출 규칙을 확인한다.
- `MypageResponseTest`에서 `PageDTO`의 모달 상태 helper와 `BookingDetailPageDTO` placeholder 계약을 확인한다.
- `MypageTemplateContractTest`에서 `mypage.mustache`, `booking-detail.mustache`가 `page` 루트 계약을 읽는지 확인한다.
- `./gradlew.bat test`로 자동 테스트를 확인한다.
- 현재 기준으로 자동 테스트는 완료됐고, 최신 예약 카드/상세 라우트 및 탈퇴 모달 상호작용에 대한 브라우저 수동 검증은 추가 확인이 필요하다.

## 의존성

- 파일: `../user/UserRepository.java`, `../user/UserService.java`, `../booking/BookingRepository.java`, `../trip/TripRepository.java`, `src/main/resources/templates/pages/mypage.mustache`, `src/main/resources/templates/pages/booking-detail.mustache`
- 기술: `Spring MVC`
