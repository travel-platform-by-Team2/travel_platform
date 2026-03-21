<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

마이페이지 메인 화면, 비밀번호 변경, 회원 탈퇴, 예약 상세 placeholder 화면을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `MypageController.java` | 마이페이지 SSR 진입, 비밀번호 변경, 탈퇴, 예약 상세 화면 |
| `MypageService.java` | 프로필/예약 카드/여행 카드 조회, 비밀번호 변경 |
| `MypageResponse.java` | 마이페이지 화면 DTO |
| `MypageBookingQueryRepository.java` | 다가오는 예약 카드 JPQL 조회 |

## 현재 구조 기준

- SSR 루트 모델 키는 `model`을 사용한다.
- `mypage.mustache`는 프로필, 예약 카드, 여행 카드처럼 컬렉션이 둘 이상이라 예외적으로 `model` 내부 컬렉션을 사용한다.
- `booking-detail.mustache`는 `model` 단건 계약을 사용한다.
- `MypageController`는 세션 사용자 확인, 서비스 호출, 화면 렌더링만 담당한다.
- `MypageService`는 프로필 조회, 예약 카드 조회, 여행 카드 조회, 비밀번호 변경 책임을 가진다.
- 예약 상세는 이번 v3에서도 placeholder 상태를 유지하고, `BookingDetailPageDTO`만 내려준다.
- `MypageResponse`의 정적 팩토리는 `createMainPage`, `fromUser`, `fromBooking`, `fromTripPlan`, `createBookingDetailPage`처럼 역할이 드러나는 이름을 사용한다.

## 정규화 메모

- 마이페이지는 `user`, `booking`, `trip`에 모두 의존한다.
- 메인 화면은 조회용 도메인이라 예약/여행 요약 구조는 별도 query repository로 분리하는 것이 우선이다.
- 예약 상세 placeholder는 실제 정규화나 구현 대상이 아니라 현재 연결 상태만 유지한다.

## 테스트

- `MypageControllerTest`
- `MypageResponseTest`
- `MypageServiceTest`
- `MypageTemplateContractTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.mypage.MypageControllerTest --tests com.example.travel_platform.mypage.MypageResponseTest --tests com.example.travel_platform.mypage.MypageServiceTest --tests com.example.travel_platform.mypage.MypageTemplateContractTest` 기준으로 맞춘다.
