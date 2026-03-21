<!-- Parent: ../AI-CONTEXT.md -->

# trip

## 목적

여행 계획의 목록, 생성, 상세, 장소 추가 흐름을 담당한다.  
SSR 화면은 `TripController`, JSON API는 `TripApiController`, 비즈니스 조립은 `TripService`가 맡는다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `TripController.java` | 여행 목록, 생성 폼, 상세, 장소 추가 SSR 화면 진입 |
| `TripApiController.java` | 여행 생성, 목록 조회, 장소 추가 API |
| `TripService.java` | 여행 목록/생성/상세/장소 추가 흐름과 DTO 조립 |
| `TripRequest.java` | 여행 생성, 장소 추가 입력 DTO |
| `TripResponse.java` | 목록/상세/생성/장소 추가 응답 DTO |
| `TripPlan.java` | 여행 계획 엔티티 |
| `TripPlace.java` | 여행 장소 엔티티 |
| `TripRepository.java` | 저장, 삭제 같은 기본 영속화 책임 |
| `TripPlanQueryRepository.java` | 목록/상세 JPQL 조회 책임 |
| `TripPlaceRepository.java` | 장소 저장, 개수 집계, 사용자 기준 삭제 |

## 현재 구조 기준

- SSR 모델 키는 `model` 단건, `models` 컬렉션 규칙을 따른다.
- `trip-list.mustache`는 `model`로 페이지 메타를 받고, 여행 카드 목록은 `models`로 받는다.
- `trip-create.mustache`, `trip-detail.mustache`, `trip-add-place.mustache`는 `model` 단건 계약을 사용한다.
- `TripController`는 세션 사용자 ID 조회, 서비스 호출, `model/models` 주입만 담당한다.
- `TripApiController`는 `sessionUser` 기준 사용자 ID를 꺼내고 `Resp.ok(...)`만 반환한다.
- `TripService`는 생성, 목록, 상세, 장소 추가 흐름을 helper 기준으로 분리한다.
- `TripService.getPlanList(...)`는 카테고리 정규화, JPQL 조회, 카드 DTO 조립, 페이지 DTO 반환 순서로 동작한다.
- `TripService.getPlanDetail(...)`는 본인 계획 검증 후 장소 정렬과 상세 DTO 조립을 담당한다.
- `TripService.addPlace(...)`는 본인 계획 검증 후 `TripPlace.create(...)`로 엔티티를 만들고 `PlaceAddedDTO`를 반환한다.
- 목록/상세/장소 추가 응답은 모두 DTO로 변환해서 반환한다.

## 정규화 메모

- `TripPlan`과 `TripPlace`의 관계는 현재 구조상 명확하다.
- `TripPlan.region`, `TripPlan.whoWith`는 문자열 코드/값 기반이라 추후 정규화 후보가 될 수 있다.
- 이번 v3에서는 정규화 후보만 식별하고 실제 Entity/DB schema 변경은 하지 않는다.

## 테스트

- `TripControllerTest`
- `TripApiControllerTest`
- `TripServiceTest`
- `TripTemplateContractTest`

최종 검증은 `./gradlew.bat test --tests com.example.travel_platform.trip.TripControllerTest --tests com.example.travel_platform.trip.TripApiControllerTest --tests com.example.travel_platform.trip.TripServiceTest --tests com.example.travel_platform.trip.TripTemplateContractTest` 기준으로 맞춘다.
