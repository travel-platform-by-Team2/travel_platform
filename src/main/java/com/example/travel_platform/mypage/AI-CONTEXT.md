<!-- Parent: ../AI-CONTEXT.md -->

# mypage

## 목적

마이페이지 메인 화면, 예약 리스트, 예약 상세, 비밀번호 변경, 회원 탈퇴 화면을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `MypageController.java` | 마이페이지 메인, 예약 리스트, 예약 상세 SSR 진입과 비밀번호 변경/회원 탈퇴 제출 처리 |
| `MypageService.java` | 프로필 조회, 예약/여행 요약 조회, 예약 리스트/상세 화면 DTO 조립, 비밀번호 변경 |
| `MypageResponse.java` | 메인/예약 리스트/예약 상세 화면 DTO |
| `BookingCategory.java` | 예약 리스트 카테고리(`전체`, `이용전`, `이용후`, `취소`) 분류 |
| `MypageQueryRepository.java` | 메인 화면용 다가오는 예약/여행 계획 요약 JPQL 조회 |

## 현재 구조 기준

- SSR 모델은 `model` 단건 규칙을 기본으로 사용한다.
- 예약 리스트 화면만 `model` + `models` 계약을 사용한다.
  - `model`: 선택 카테고리, 빈 상태, 이동 링크 같은 페이지 메타
  - `models`: 예약 카드 목록
- `mypage.mustache`는 메인 화면이다.
- `booking-list.mustache`는 예약 리스트 화면이다.
- `booking-detail.mustache`는 예약 상세 화면이다.
- 예약 리스트/상세의 실제 데이터는 `bookingService.getBookingList(...)`, `bookingService.getBookingDetail(...)`를 사용한다.
- 메인 화면 요약용 예약/여행 계획 조회는 `MypageQueryRepository` 하나로 모아둔다.

## 책임 경계

- `mypage`는 예약 도메인 규칙을 직접 갖지 않는다.
- 예약 취소, 예약 목록, 예약 상세의 실제 데이터 규칙은 `booking` 도메인이 가진다.
- `mypage`는 그 결과를 화면 DTO로 변환하고 SSR 계약만 맞춘다.
- 표현 전용 상태 텍스트나 CSS 클래스는 가능한 한 템플릿에서 처리하고, 백엔드는 표시용 boolean과 핵심 값 위주로 내려준다.

## 템플릿 규칙

- 추가 HTML 시안은 현재 프로젝트 공용 헤더/푸터 partial에 맞춰 감싼다.
- `mypage.mustache` 예약 섹션 헤더에는 `전체보기` 링크가 있다.
- `booking-list.mustache`는 카테고리 탭과 예약 카드 목록을 렌더링한다.
- `booking-detail.mustache`는 예약 목록으로 돌아가기 링크, 마이페이지로 돌아가기 버튼, 취소 버튼을 가진다.

## 테스트

- `MypageControllerTest`
- `MypageServiceTest`
- `MypageResponseTest`
- `MypageTemplateContractTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.mypage.MypageControllerTest --tests com.example.travel_platform.mypage.MypageServiceTest --tests com.example.travel_platform.mypage.MypageResponseTest --tests com.example.travel_platform.mypage.MypageTemplateContractTest` 와 `./gradlew.bat test` 기준으로 맞춘다.
