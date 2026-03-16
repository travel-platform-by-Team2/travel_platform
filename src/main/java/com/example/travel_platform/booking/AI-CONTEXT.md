<!-- Parent: ../AI-CONTEXT.md -->

# booking

## 목적

숙소 예약, 지도 상세 화면, 예약 API, 지도 보조 데이터 가공 로직을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| Booking.java | 예약 엔티티다. |
| BookingApiController.java | 예약 관련 REST API를 제공한다. |
| BookingController.java | 지도 상세, 체크아웃, 예약 완료 화면을 렌더링한다. |
| BookingRepository.java | 예약 저장/조회 저장소다. |
| BookingRequest.java | 예약 입력 DTO를 정의한다. |
| BookingResponse.java | 예약 응답 DTO를 정의한다. |
| BookingService.java | 예약 TODO 로직과 지도 POI/image 보조 가공을 처리한다. |
| LodgingQueryRepository.java | 숙소 조회 관련 저장소다. |
| MapPlaceImageRepository.java | 장소 이미지 조회 관련 저장소다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 지도 상세, 체크아웃, 예약 완료 화면은 템플릿 변수명과 쿼리 파라미터 이름이 강하게 연결되어 있다.
- `BookingApiController`의 예약 생성/조회/취소는 아직 `userId=1` 고정 placeholder를 사용하므로 실제 인증 연결 시 API 계약을 같이 정리한다.
- `mergeMapPois`는 Kakao POI와 DB 숙소 목록을 합치는 보조 API이고, `getPlaceImage`는 DB 캐시 + Kakao 페이지 scraping 순서로 이미지를 찾는다.
- 저장소와 서비스 일부에 TODO가 남아 있으므로 실제 영속성 추가 시 API 계약과 화면 흐름을 함께 맞춘다.
- `KAKAO_MAP_APP_KEY`와 외부 이미지 URL은 환경 변수/외부 데이터에서 들어오므로 null 방어를 유지한다.

## 테스트

- `/bookings/map-detail`, `/bookings/checkout`, `/bookings/complete` 화면과 `/api/bookings`, `/api/bookings/place-image`, `/api/bookings/map-pois/merge` 응답을 함께 점검한다.

## 의존성

- 내부: `trip`, `src/main/resources/templates/pages`, `src/main/resources/static/js/map-detail.js`
- 외부: `Spring MVC`, `JPA/Hibernate`, `Kakao Map JS SDK`, `Jsoup`
