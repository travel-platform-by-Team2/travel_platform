<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

마이페이지 메인 화면, 비밀번호 변경과 회원 탈퇴, 예약 상세 placeholder 화면을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `MypageController.java` | 마이페이지 SSR 진입, 비밀번호 변경 제출, 회원 탈퇴 제출, 예약 상세 placeholder 화면 |
| `MypageService.java` | 프로필 조회, 예약 요약 조회, 여행 요약 조회, 비밀번호 변경 |
| `MypageResponse.java` | 마이페이지 화면 DTO |
| `MypageBookingQueryRepository.java` | 다가오는 예약 요약과 예약 소유 확인 JPQL 조회 |
| `MypageTripPlanQueryRepository.java` | 다가오는 여행 요약 JPQL 조회 |

## 현재 구조 기준

- SSR 루트 모델은 `model`을 사용한다.
- `mypage.mustache`는 `model.profile`, `model.bookingSection`, `model.tripPlanSection`처럼 화면 섹션 단위로 값을 나눠 사용한다.
- `booking-detail.mustache`는 `model` 단건 계약을 사용한다.
- `MypageController`는 세션 사용자 확인, 서비스 호출, 화면 렌더링만 담당한다.
- `MypageService`는 프로필 조회, 예약 요약 조회, 여행 요약 조회, 비밀번호 변경 책임을 가진다.
- 예약 상세는 placeholder 상태를 유지하되, `existsOwnedBooking(...)`으로 현재 사용자 소유 예약인지 먼저 확인한다.
- `MypageResponse`의 정적 팩토리는 `createMainPage`, `fromUserEntity`, `createBookingSummaryCard`, `createTripPlanSummaryCard`, `createBookingDetailPlaceholderPage`처럼 역할이 드러나는 이름을 사용한다.

## 정규화 메모

- 마이페이지는 `user`, `booking`, `trip`을 모두 참조한다.
- 메인 화면 조회는 참조 도메인이 많아서 예약/여행 요약 구조를 별도 query repository로 분리하고, 엔티티 전체 대신 요약 row로 조립한다.
- 예약 상세 placeholder는 실제 정규화나 구현 대상이 아니고 현재 연결 상태와 소유 확인만 유지한다.

## 테스트

- `MypageControllerTest`
- `MypageResponseTest`
- `MypageServiceTest`
- `MypageTemplateContractTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.mypage.MypageControllerTest --tests com.example.travel_platform.mypage.MypageResponseTest --tests com.example.travel_platform.mypage.MypageServiceTest --tests com.example.travel_platform.mypage.MypageTemplateContractTest` 기준으로 맞춘다.
