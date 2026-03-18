<!-- Parent: ../AI-CONTEXT.md -->

# booking

## 목적

숙소 예약, 지도 상세 화면, 예약 API, 지도 보조 데이터 가공 로직을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `Booking.java` | 예약 엔티티 |
| `BookingApiController.java` | 예약 관련 REST API |
| `BookingController.java` | 지도 상세, 체크아웃, 예약 완료 SSR 화면 라우팅 |
| `BookingRepository.java` | 예약 저장 및 조회 리포지토리 |
| `BookingRequest.java` | 예약 입력 DTO |
| `BookingResponse.java` | 예약 응답 DTO |
| `BookingService.java` | 예약 TODO 로직과 지도 POI/image 보조 가공 처리 |
| `LodgingQueryRepository.java` | 숙소 조회 전용 리포지토리 |
| `MapPlaceImageRepository.java` | 장소 이미지 조회/캐시 리포지토리 |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 지도 상세, 체크아웃, 예약 완료 화면은 템플릿 변수명과 쿼리 파라미터 이름이 강하게 연결돼 있다.
- `BookingApiController`의 예약 생성/조회/취소는 아직 `userId=1` placeholder를 사용하므로 실제 인증 연동 시 API 계약을 함께 정리한다.
- `mergeMapPois`는 Kakao POI와 DB 숙소 목록을 합치는 보조 API이고, `getPlaceImage`는 DB 캐시 + Kakao 페이지 scraping 순서로 이미지를 찾는다.
- `BookingService.processBookingCompletion(...)`은 새 여행 계획이 필요할 때 `TripPlan.create(...)`를 사용하고, `BookingController`가 넘기는 `regionKey`를 저장한다.
- 예약 완료 화면의 `region` 문자열은 표시용이고, 실제 `TripPlan.region`에는 길이/매핑 제약을 고려해 `regionKey`를 넣는다.

## 테스트

- `/bookings/map-detail`, `/bookings/checkout`, `/bookings/complete` 화면과 `/api/bookings`, `/api/bookings/place-image`, `/api/bookings/map-pois/merge` 응답을 함께 점검한다.
- 예약 완료 시 새 `TripPlan` 생성 경로가 `region` null/길이 문제 없이 동작하는지 확인한다.

## 의존

- 도메인: `trip`, `src/main/resources/templates/pages`, `src/main/resources/static/js/map-detail.js`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`, `Kakao Map JS SDK`, `Jsoup`
