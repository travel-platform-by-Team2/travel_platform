<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

마이페이지 메인 화면, 예약 목록, 예약 상세, 비밀번호 변경, 회원 탈퇴 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `MypageController.java` | `/mypage`, `/mypage/bookings`, `/mypage/bookings/{bookingId}` SSR 진입과 비밀번호 변경/탈퇴 요청을 처리한다. |
| `MypageService.java` | 메인/예약 화면 조회와 메인 화면 상태별 DTO 조립, 비밀번호 변경 검증을 담당한다. |
| `MypageResponse.java` | 마이페이지 화면 전용 DTO를 정의한다. |
| `BookingCategory.java` | 예약 목록 필터 카테고리 분류를 담당한다. |
| `MypageQueryRepository.java` | 메인 화면에 필요한 예약/여행 계획 요약 row를 조회한다. |

## 현재 구조 기준

- SSR 루트 속성은 기본적으로 `model`을 사용한다.
- 예약 목록 화면은 `model + models` 계약을 사용한다.
  - `model`: 필터 상태, 페이지 메타
  - `models`: 예약 카드 목록
- 메인 화면 예약/여행 계획 요약은 `MypageQueryRepository`에서 조회한다.
- 예약 목록/상세의 실제 예약 데이터는 `bookingService.getBookingList(...)`, `bookingService.getBookingDetail(...)`를 사용한다.
- `MypageController`는 `MainPageDTO`를 다시 수정하지 않고, `MypageService`가 상태가 반영된 DTO를 반환하면 그대로 `model`에 담는다.

## DTO 조립 규칙

- `MypageResponse.MainPageDTO`는 `createMainPage`, `createPasswordSuccessPage`, `createPasswordFailurePage`, `createWithdrawFailurePage` 팩토리로 메인 화면 상태를 만든다.
- 비밀번호 성공/실패, 탈퇴 실패 같은 화면 상태는 컨트롤러 후처리가 아니라 서비스 조립 단계에서 결정한다.
- `BookingListPageDTO`, `BookingDetailPageDTO` 같은 화면 DTO는 링크, 표시 문구, 파생 필드를 DTO 내부에서 계산한다.

## 책임 경계

- `mypage`는 예약 비즈니스 규칙 자체를 소유하지 않는다.
- 예약 목록/상세/취소 가능 여부 판단 등 예약 도메인 규칙은 `booking`이 담당한다.
- `mypage`는 그 결과를 화면용 DTO로 변환하고 SSR 계약에 맞춰 전달한다.

## 테스트

- `MypageControllerTest`
- `MypageServiceTest`
- `MypageResponseTest`
- `MypageTemplateContractTest`

검증은 `./gradlew.bat test --tests com.example.travel_platform.mypage.MypageControllerTest --tests com.example.travel_platform.mypage.MypageServiceTest --tests com.example.travel_platform.mypage.MypageResponseTest --tests com.example.travel_platform.mypage.MypageTemplateContractTest` 와 `./gradlew.bat test` 기준으로 맞춘다.
