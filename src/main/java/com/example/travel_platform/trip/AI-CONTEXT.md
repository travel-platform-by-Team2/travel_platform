<!-- Parent: ../AI-CONTEXT.md -->

# trip

## 목적

여행 계획 생성, 목록, 상세, 장소 추가 기능의 SSR 화면 흐름과 API 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `TripController.java` | 여행 계획 목록, 생성, 상세, 장소 추가 SSR 화면 진입 |
| `TripApiController.java` | 여행 계획 생성, 목록 조회, 장소 추가 JSON API |
| `TripService.java` | 여행 계획 비즈니스 로직과 화면/API 응답 DTO 조립 |
| `TripRequest.java` | 여행 계획 생성, 장소 추가 입력 DTO |
| `TripResponse.java` | 목록/상세/생성/장소 추가 화면 DTO와 API 응답 DTO |
| `TripPlan.java` | 여행 계획 엔티티, `TripPlan.create(...)` 생성 경로 사용 |
| `TripPlace.java` | 여행 장소 엔티티, `TripPlace.create(...)` 생성 경로 사용 |
| `TripRepository.java` | 여행 계획 목록/상세 조회 |
| `TripPlaceRepository.java` | 여행 장소 저장과 계획별 장소 수 집계 |

## AI 작업 지침

- `/trip`, `/trip/create`, `/trip/place`는 `TripController`가 최상위 `page` 모델로 Mustache에 전달한다.
- `/trip/detail`은 현재 템플릿 계약에 맞춰 `TripController`가 `plan` 모델로 전달한다.
- `TripController`와 `TripApiController`는 모두 `sessionUser` 기준으로 사용자 ID를 꺼내고, 서비스에 식별자만 전달한다.
- `TripService`는 목록/생성/상세/장소 추가 흐름을 helper 기준으로 나눠 읽히게 정리돼 있다.
- 목록/생성은 로그인 필요, 상세/장소 추가는 로그인 + 본인 계획만 허용한다.
- `TripService.getPlanList(...)`는 카테고리 정규화, 조회 경로 선택, 카드 DTO 조립, 페이지 DTO 반환 순서로 동작한다.
- `TripService.createPlan(...)`은 검증, 사용자 조회, 엔티티 생성, 저장, 생성 응답 DTO 반환 순서로 동작한다.
- `TripService.getPlanDetail(...)`은 소유자 계획 조회, 장소 정렬, 상세 DTO 조립 순서로 동작한다.
- `TripService.getPlacePage(...)`는 `PlacePageDTO`로 `detail`, `saveUrl`, `detailUrl`, `existingCount`, `kakaoMapAppKey`를 함께 반환한다.
- `TripService.addPlace(...)`는 `TripPlace.create(...)`로 엔티티를 만들고 `PlaceAddedDTO`로 `planId`, `placeCount`, `detailUrl`을 반환한다.
- `TripResponse.ListPageDTO`는 목록 상태(`isResult`, `isUpcoming`, `isPast`)와 페이지 메타데이터를 함께 가진다.
- `TripResponse.DetailDTO`는 `formattedTitle`, `regionLabel`, `whoWithLabel`, `travelPeriodLabel`, `places`, `placeCount`, `hasPlaces`를 가진다.
- `TripResponse.CreateFormDTO`는 생성 화면 입력값과 validation 에러 메시지를 함께 가진다.
- `trip-create.mustache`는 `page.title`, `page.region`, `page.whoWith`, `page.startDate`, `page.endDate`와 각 에러 메시지를 재주입한다.
- `trip-list.mustache`, `trip-create.mustache`는 챗봇 패널을 포함하므로 `{{>partials/chatbot-assets}}`를 같이 유지한다.
- `trip-detail.mustache`는 `plan.formattedTitle`, `plan.regionLabel`, `plan.travelPeriodLabel`, `plan.whoWithLabel`을 사용한다.
- `trip-add-place.mustache`와 `trip-add-place.js`는 `saveUrl`, `detailUrl`, `existingCount`, `page.kakaoMapAppKey` 계약을 유지해야 한다.
- 미완성 UI나 더미 콘텐츠를 리팩토링 중 임의로 완성하지 않는다.

## 테스트

- `TripControllerTest`는 SSR view name과 model key 계약을 검증한다.
- `TripApiControllerTest`는 JSON 응답과 로그인 필요 예외를 검증한다.
- `TripServiceTest`는 생성/목록/상세/장소 추가 흐름과 권한 규칙을 검증한다.
- `TripTemplateContractTest`는 `trip-*` Mustache 템플릿의 모델 계약을 검증한다.
- 최종 검증은 `./gradlew.bat test` 기준으로 맞춘다.

## 의존

- 도메인: `user`
- 템플릿: `src/main/resources/templates/pages/trip-*`
- 정적 자산: `src/main/resources/static/js/trip-add-place.js`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`
