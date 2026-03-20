<!-- Parent: ../AI-CONTEXT.md -->

# booking

## 목적

숙소 예약, 지도 상세 화면, 예약 API, 지도 보조 데이터 가공 로직을 담당한다.

## 주요 파일

| 파일명                      | 설명                                                  |
| --------------------------- | ----------------------------------------------------- |
| `Booking.java`              | 예약 엔티티                                           |
| `BookingApiController.java` | 예약 관련 REST API                                    |
| `BookingController.java`    | 지도 상세, 체크아웃, 예약 완료 SSR 화면 라우팅        |
| `BookingRepository.java`    | 예약, 숙소 POI 조회, 장소 이미지 캐싱 통합 리포지토리 |
| `BookingRequest.java`       | 예약 입력 DTO                                         |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 지도 상세, 체크아웃, 예약 완료 화면은 현재 `page` 루트 DTO 계약을 사용하므로 템플릿 변경 시 `BookingController`, `BookingResponse`, Mustache 템플릿을 같이 본다.
- `map-detail.mustache`는 Kakao SDK 다음에 `/js/map-detail.js`를 직접 로드하고, 공통 `scripts.mustache`는 더 이상 지도 전용 JS를 싣지 않는다.
- `BookingApiController`의 예약 생성/조회/취소는 아직 `PLACEHOLDER_USER_ID = 1` 기준 placeholder 흐름을 유지한다.
- `BookingRepository`는 `mypage`의 다가오는 예약 카드 조회에도 재사용되므로, 예약 정렬/필터 기준 변경 시 마이페이지 영향 범위를 같이 본다.
- `mergeMapPois`는 Kakao POI와 DB 숙소 목록을 합치는 보조 API이고, `getPlaceImage`는 `DB 캐시 -> TourAPI -> Kakao 페이지 scraping` 순서로 이미지를 찾는다.
- `BookingService.processBookingCompletion(...)`은 `BookingRequest.CompleteBookingDTO`를 받고, 새 여행 계획이 필요할 때 `TripPlan.create(...)`를 사용한다.
- 예약 완료 화면의 `region` 문자열은 표시용이고, 실제 `TripPlan.region`에는 길이/매핑 제약을 고려해 `regionKey`를 넣는다.
- 예약 CRUD 중 `cancelBooking`, `getBookingList`, `getBookingDetail`은 아직 no-op, 빈 리스트, null 상세 placeholder 상태다.

## 테스트

- `BookingControllerTest`, `BookingResponseTest`, `BookingTemplateContractTest`로 `/bookings/map-detail`, `/bookings/checkout`, `/bookings/complete`의 SSR 계약을 점검한다.
- `StaticScriptContractTest`로 지도 상세의 전용 script include 위치도 함께 점검한다.
- `BookingApiControllerTest`로 `/api/bookings`, `/api/bookings/place-image`, `/api/bookings/map-pois/merge`의 현재 응답 계약과 placeholder CRUD 흐름을 점검한다.
- `BookingServiceTest`로 예약 완료 시 새 `TripPlan` 생성 경로와 placeholder CRUD 상태를 함께 점검한다.

## 의존

- 도메인: `trip`, `src/main/resources/templates/pages`, `src/main/resources/static/js/map-detail.js`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`, `Kakao Map JS SDK`, `Jsoup`
