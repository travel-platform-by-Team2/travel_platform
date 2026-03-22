<!-- Parent: ../AI-CONTEXT.md -->

# booking

## 목적

숙소 예약 SSR 화면, 예약 API, 지도 보조 API, 예약 완료 후 저장 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `BookingController.java` | 지도 상세, 체크아웃, 예약 완료 SSR 화면 |
| `BookingApiController.java` | 예약 CRUD API, 객실/이미지/POI 보조 API |
| `BookingService.java` | 예약 완료 저장, 예약 생성, 예약 목록/상세/취소, 보조 API |
| `BookingRequest.java` | 예약/지도 보조 API 입력 DTO |
| `BookingResponse.java` | SSR/API 응답 DTO |
| `Booking.java` | 예약 엔티티 |
| `BookingRepository.java` | 예약 저장, 삭제 같은 기본 영속화 |
| `BookingQueryRepository.java` | 예약 목록/상세 JPQL 조회 |
| `LodgingQueryRepository.java` | 숙소 POI JPQL 조회 |
| `MapPlaceImageRepository.java` | 장소 이미지 캐시 조회/업서트 |

## 현재 구조 기준

- SSR 모델 키는 `model` 단건 규칙을 사용한다.
- `map-detail.mustache`, `booking-checkout.mustache`, `booking-complete.mustache`는 모두 `model` 기준으로 렌더링한다.
- `BookingController`는 파라미터 정리, 세션 사용자 확인, DTO 조립, 화면 렌더링만 담당한다.
- `BookingApiController`는 `sessionUser` 기준으로 사용자 ID를 꺼내고 `Resp.ok(...)`만 반환한다.
- `BookingResponse`의 정적 팩토리는 `createMapDetailPage`, `createCheckoutPage`, `createCompletePage`, `createRoom`, `createPlaceImage`처럼 역할이 드러나는 이름을 사용한다.
- `BookingService`는 예약 완료 저장, 예약 생성, 예약 목록/상세/취소, 이미지 조회, POI 병합을 helper 기준으로 분리한다.
- `cancelBooking(...)`은 예약 삭제가 아니라 `BookingStatus`, `cancelledAt` 기준 상태 변경으로 처리한다.
- `getBookingList(...)`, `getBookingDetail(...)`은 `BookingQueryRepository`의 JPQL 결과를 DTO로 조립한다.
- 예약 목록 조회는 `Booking` 자체 필드만 사용하므로 `tripPlan`을 fetch 하지 않는다.
- 예약 상세 조회는 `tripPlanId` 접근이 있어서 단건 조회에서만 `tripPlan` fetch를 유지한다.
- `mergeMapPois(...)`는 `LodgingQueryRepository`의 JPQL 결과와 Kakao POI를 합친다.
- 장소 이미지 캐시는 `MapPlaceImageRepository`를 통해 `캐시 조회 -> 외부 조회 -> 업서트` 순서로 처리한다.
- 예약 완료 시 여행 계획이 없으면 `TripPlan.create(...)`로 최소 계획을 만들고 저장한다.
- `Booking`은 `lodgingName`과 `roomName`을 분리해서 저장한다.
- 지역은 자유 문자열 `location` 대신 `regionKey` 코드로 저장하고, 화면 표시용 지역명은 `Booking.getLocation()`과 `BookingResponse`에서 라벨로 제공한다.
- 예약 상태는 `BookingStatus` enum 문자열로 저장하고, 취소 시각은 `cancelledAt`에 남긴다.
- `BookingRequest.CreateBookingDTO`, `BookingRequest.CompleteBookingDTO`는 `roomName`, `regionKey`를 직접 받는다.
- `booking-checkout.mustache`는 hidden input으로 `regionKey`를 함께 넘긴다.
- `booking-detail.mustache`는 `mypage`에서 사용하는 실제 예약 상세 화면이며, 취소 버튼은 `DELETE /api/bookings/{bookingId}`를 호출한다.

## 정규화 메모

- `Booking`은 `TripPlan`, `User`를 함께 참조하므로 예약 상세/마이페이지와 강하게 연결된다.
- `lodgingName`은 숙소 이름, `roomName`은 객실 이름으로 나눠 저장한다.
- `location` 자유 문자열 저장은 제거하고 `regionKey` 코드 저장으로 전환했다.
- `status`, `cancelledAt`은 예약 이력 유지를 위한 최소 상태 구조다.
- `imageUrl`은 외부 연동 보조 데이터라 nullable 표시 값으로 유지한다.
- `Lodging`, `MapPlaceImage`는 지도 보조 데이터 저장 구조라 실제 정규화는 승인 단위로 분리한다.

## 테스트

- `BookingControllerTest`
- `BookingApiControllerTest`
- `BookingServiceTest`
- `BookingResponseTest`
- `BookingTemplateContractTest`
- `BookingRepositoryUpsertTest`
- `ChatSchemaProviderTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.booking.BookingControllerTest --tests com.example.travel_platform.booking.BookingApiControllerTest --tests com.example.travel_platform.booking.BookingServiceTest --tests com.example.travel_platform.booking.BookingResponseTest --tests com.example.travel_platform.booking.BookingTemplateContractTest --tests com.example.travel_platform.booking.BookingRepositoryUpsertTest --tests com.example.travel_platform.chatbot.application.ChatSchemaProviderTest`와 `./gradlew.bat test` 기준으로 맞춘다.
