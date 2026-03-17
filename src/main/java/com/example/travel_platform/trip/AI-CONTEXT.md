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
| `TripRepository.java` | 사용자별 여행 계획 목록, 카테고리, 페이지 조회 |
| `TripRequest.java` | 여행 계획 입력 DTO |
| `TripResponse.java` | 목록 페이지 DTO, 페이지 번호 DTO, 상세 DTO |
| `TripService.java` | 여행 계획 비즈니스 로직과 목록 DTO 조립 |

## 하위 디렉터리

- 없음

## AI 작업 지침

- `/trip` 목록 화면은 `TripController`가 최상위 `page` DTO 하나만 Mustache에 전달한다.
- `TripResponse.PlanListPageDTO` 안에 목록 데이터와 탭 상태(`isResult`, `isUpcoming`, `isPast`), 페이지 정보가 함께 들어가며 `PlanListPageDTO.of(...)`가 페이징 메타데이터 생성까지 담당한다.
- `TripResponse.PlanSummaryDTO`, `PlaceDTO`, `PlanDetailDTO`는 서비스에서 builder 나열로 만들지 않고 생성자로 조립한다.
- `trip-list.mustache`는 루트 `tripPlans`, `pageDTO`, 카테고리 플래그를 읽지 않고 `page` 기준으로만 렌더링한다.
- `TripService.getPlanList(...)`는 카테고리별 조회 선택과 카드 DTO 변환에 집중하고, 페이지 메타데이터 생성은 `TripResponse.PlanListPageDTO.of(...)`로 넘긴 상태다.
- `TripService.createPlan(...)`은 엔티티를 setter로 조립하지 않고 `TripPlan.create(...)`를 사용한다.
- `TripService.addPlace(...)`도 엔티티를 setter로 조립하지 않고 `TripPlace.create(...)`를 사용한다.
- 엔티티가 `TripRequest` 같은 웹 DTO를 직접 참조하지 않도록 하고, 서비스는 엔티티 생성에 필요한 값만 넘긴다.
- 상세/장소 추가 화면은 아직 `plan`, `kakaoMapAppKey`를 별도로 사용하므로 목록 화면 패턴과는 동일하지 않다.
- `TripApiController`는 API에서 여전히 `userId=1` placeholder가 남아 있으므로 실제 인증 연동 전에는 SSR/API 흐름을 함께 점검한다.

## 테스트

- `/trip` 목록 화면에서 카테고리 탭과 페이지 링크가 올바르게 유지되는지 확인한다.
- 목록이 비었을 때 empty state가 정상 렌더링되는지 확인한다.
- `TripPlan.create(...)`, `TripPlace.create(...)`를 사용하는 생성 경로와 상세/장소 추가 화면의 기존 바인딩이 영향을 받지 않는지 확인한다.

## 의존

- 도메인: `user`, `src/main/resources/templates/pages/trip-*`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`
