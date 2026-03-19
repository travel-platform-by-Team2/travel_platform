<!-- Parent: ../AI-CONTEXT.md -->

# trip

## 목적

여행 계획 생성, 목록, 상세, 장소 추가 기능의 SSR 화면 흐름과 API를 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `TripApiController.java` | 여행 계획 생성, 목록 조회, 장소 추가 REST API |
| `TripController.java` | 여행 계획 목록/생성/상세/장소 추가 SSR 화면 라우트 |
| `TripPlace.java` | 여행 장소 엔티티. 생성은 `TripPlace.create(...)`로 처리 |
| `TripPlan.java` | 여행 계획 엔티티. 생성은 `TripPlan.create(...)`로 처리 |
| `TripRepository.java` | `TripPlan` 저장과 사용자별 여행 계획 목록/상세 조회 |
| `TripPlaceRepository.java` | `TripPlace` 저장과 계획별 장소 수 집계 |
| `TripRequest.java` | 여행 계획 입력 DTO |
| `TripResponse.java` | 목록/상세/생성 폼/장소 추가 화면 DTO와 API 응답 DTO |
| `TripService.java` | 여행 계획 비즈니스 로직과 페이지/응답 DTO 조립 |

## 하위 디렉터리

- 없음

## AI 작업 지침

- `/trip` 목록 화면은 `TripController`가 최상위 `page` DTO 하나만 Mustache에 전달한다.
- `/trip/place`는 `TripController`가 최상위 `page` DTO 하나만 Mustache에 전달하고, `/trip/detail`은 현재 템플릿 계약에 맞춰 `plan` 모델을 전달한다.
- `TripResponse.ListPageDTO` 안에 목록 데이터와 탭 상태(`isResult`, `isUpcoming`, `isPast`), 페이지 정보가 함께 들어가며 `ListPageDTO.of(...)`가 페이징 메타데이터 생성까지 담당한다.
- `TripResponse.SummaryDTO`, `DetailDTO`, `DetailPageDTO`, `PlacePageDTO`, `CreateFormDTO`가 현재 템플릿 계약을 유지하는 출력 모델이다.
- `trip-list.mustache`는 루트 `tripPlans`, `pageDTO`, 카테고리 플래그를 읽지 않고 `page` 기준으로만 렌더링한다.
- `TripService.getPlanList(...)`는 카테고리별 조회 선택과 카드 DTO 변환에 집중하고, 페이지 메타데이터 생성은 `TripResponse.ListPageDTO.of(...)`로 넘긴 상태다.
- `TripService.getPlacePage(...)`가 장소 추가 화면의 최상위 DTO를 반환하고, 상세 화면은 현재 템플릿 계약에 맞춰 `TripService.getPlanDetail(...)` 결과를 `plan`으로 전달한다.
- `TripService.createPlan(...)`은 엔티티를 setter로 조립하지 않고 `TripPlan.create(...)`를 사용한다.
- `TripService.addPlace(...)`도 엔티티를 setter로 조립하지 않고 `TripPlace.create(...)`를 사용한다.
- `SummaryDTO`와 `DetailDTO`가 지역 라벨, 동행 라벨, 기본 이미지, D-day 같은 출력 포맷팅 일부를 내부에서 계산한다.
- 엔티티가 `TripRequest` 같은 웹 DTO를 직접 참조하지 않도록 하고, 서비스는 엔티티 생성에 필요한 값만 넘긴다.
- `trip` 템플릿은 더미 데이터와 임시 구조가 섞여 있는 작업 중 상태이므로, 리팩토링 중에도 템플릿 구조를 임의로 바꾸지 않는다.
- `trip-add-place.mustache`와 `trip-add-place.js`는 `saveUrl`, `detailUrl`, `existingCount`, `page.kakaoMapAppKey` 계약을 함께 유지해야 한다.

## 테스트

- `/trip` 목록 화면에서 카테고리 탭과 페이지 링크가 올바르게 유지되는지 확인한다.
- 목록이 비었을 때 empty state가 정상 렌더링되는지 확인한다.
- 여행 계획 생성/상세/장소 추가 흐름이 현재 템플릿 구조를 유지한 채 정상 동작하는지 확인한다.
- `TripPlan.create(...)`, `TripPlace.create(...)`를 사용하는 생성 경로와 상세/장소 추가 화면의 기존 바인딩이 영향을 받지 않는지 확인한다.
- `./gradlew.bat test`는 통과했고, 브라우저 수동 검증은 별도로 남아 있다.

## 의존

- 도메인: `user`, `src/main/resources/templates/pages/trip-*`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`
