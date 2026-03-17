<!-- Parent: ../AI-CONTEXT.md -->

# trip

## 목적

여행 계획 생성, 목록, 상세, 장소 추가 기능의 SSR 화면 흐름과 API를 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `TripApiController.java` | 여행 계획 생성, 목록 조회, 장소 추가 REST API |
| `TripController.java` | 여행 계획 목록/생성/상세/장소 추가 SSR 화면 라우팅 |
| `TripPlace.java` | 여행 장소 엔티티 |
| `TripPlan.java` | 여행 계획 엔티티 |
| `TripRepository.java` | 사용자별 여행 계획 목록, 카테고리, 페이징 조회 |
| `TripRequest.java` | 여행 계획 입력 DTO |
| `TripResponse.java` | 목록 페이지 DTO, 페이지 번호 DTO, 상세 DTO |
| `TripService.java` | 여행 계획 비즈니스 로직과 목록 DTO 조립 |

## 하위 디렉토리

- 없음

## AI 작업 지침

- `/trip` 목록 화면은 `TripController`가 최상위 `page` DTO 하나만 Mustache에 전달한다.
- `TripResponse.PlanListPageDTO` 안에 목록 데이터와 탭 상태(`isResult`, `isUpcoming`, `isPast`), 페이징 정보를 함께 넣는다.
- `trip-list.mustache`는 루트 `tripPlans`, `pageDTO`, 카테고리 플래그를 섞어 쓰지 않고 `page` 기준으로만 렌더링한다.
- 카테고리 값은 `TripService.getPlanList(...)`에서 `result`, `upcoming`, `past` 중 하나로 정규화한다.
- 상세/장소 추가 화면은 아직 `plan`, `kakaoMapAppKey`를 별도로 사용하므로 목록 화면 패턴과 혼동하지 않는다.
- `TripApiController`의 API는 여전히 `userId=1` placeholder가 남아 있으므로 실제 인증 연동 시 SSR/API 흐름을 함께 점검한다.

## 테스트

- `/trip` 목록 화면에서 카테고리 탭과 페이징 링크가 올바르게 유지되는지 확인한다.
- 목록이 비었을 때 empty state가 정상 렌더링되는지 확인한다.
- 상세/장소 추가 화면의 기존 `plan`, `kakaoMapAppKey` 바인딩이 영향을 받지 않는지 확인한다.

## 의존

- 도메인: `user`, `src/main/resources/templates/pages/trip-*`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`
