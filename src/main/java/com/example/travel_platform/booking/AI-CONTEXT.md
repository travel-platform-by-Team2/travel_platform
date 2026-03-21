<!-- Parent: ../AI-CONTEXT.md -->

# booking

## 목적

숙소 예약 SSR 화면, 예약 API, 지도 보조 API, 예약 완료 후 저장 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `BookingController.java` | 지도 상세, 체크아웃, 예약 완료 SSR 화면 |
| `BookingApiController.java` | 예약 CRUD placeholder, 객실/이미지/POI 보조 API |
| `BookingService.java` | 예약 완료 저장, 예약 생성, 보조 API, placeholder CRUD |
| `BookingRequest.java` | 예약/지도 보조 API 입력 DTO |
| `BookingResponse.java` | SSR/API 응답 DTO |
| `Booking.java` | 예약 엔티티 |
| `BookingRepository.java` | 예약 저장, 삭제 같은 기본 영속화 |
| `LodgingQueryRepository.java` | 숙소 POI JPQL 조회 |
| `MapPlaceImageRepository.java` | 장소 이미지 캐시 조회/업서트 |

## 현재 구조 기준

- SSR 모델 키는 `model` 단건 규칙을 사용한다.
- `map-detail.mustache`, `booking-checkout.mustache`, `booking-complete.mustache`는 모두 `model` 기준으로 렌더링한다.
- `BookingController`는 파라미터 정리, 세션 사용자 확인, DTO 조립, 화면 렌더링만 담당한다.
- `BookingApiController`는 `sessionUser` 기준으로 사용자 ID를 꺼내고 `Resp.ok(...)`만 반환한다.
- `BookingResponse`의 정적 팩토리는 `createMapDetailPage`, `createCheckoutPage`, `createCompletePage`, `createRoom`, `createPlaceImage`처럼 역할이 드러나는 이름을 사용한다.
- `BookingService`는 예약 완료 저장, 예약 생성, 이미지 조회, POI 병합, placeholder CRUD를 helper 기준으로 분리한다.
- `cancelBooking`, `getBookingList`, `getBookingDetail`은 아직 placeholder 상태이며 이번 v3에서도 완성하지 않는다.
- `mergeMapPois(...)`는 `LodgingQueryRepository`의 JPQL 결과와 Kakao POI를 합친다.
- 장소 이미지 캐시는 `MapPlaceImageRepository`를 통해 `캐시 조회 -> 외부 조회 -> 업서트` 순서로 처리한다.
- 예약 완료 시 여행 계획이 없으면 `TripPlan.create(...)`로 최소 계획을 만들고 저장한다.

## 정규화 메모

- `Booking`은 `TripPlan`, `User`를 함께 참조하므로 예약 상세/마이페이지와 강하게 연결된다.
- `location`, `lodgingName`, `imageUrl`은 외부 연동 데이터라 정규화 후보가 될 수 있지만 이번 차수에서는 구조 변경 없이 유지한다.
- `Lodging`, `MapPlaceImage`는 지도 보조 데이터 저장 구조라 실제 정규화는 승인 단위로 분리한다.

## 테스트

- `BookingControllerTest`
- `BookingApiControllerTest`
- `BookingServiceTest`
- `BookingResponseTest`
- `BookingTemplateContractTest`
- `BookingRepositoryUpsertTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.booking.BookingControllerTest --tests com.example.travel_platform.booking.BookingApiControllerTest --tests com.example.travel_platform.booking.BookingServiceTest --tests com.example.travel_platform.booking.BookingResponseTest --tests com.example.travel_platform.booking.BookingTemplateContractTest --tests com.example.travel_platform.booking.BookingRepositoryUpsertTest` 기준으로 맞춘다.
