<!-- Parent: ../AI-CONTEXT.md -->

# trip

## 목적

여행 계획 생성, 목록, 상세, 장소 추가 기능의 화면/API/도메인과 목록 페이지 페이징 조회를 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| TripApiController.java | 여행 계획 생성, 목록 조회, 장소 추가 REST API를 제공한다. |
| TripController.java | 여행 계획 목록/생성/상세/장소 추가 화면 라우팅을 담당한다. |
| TripPlace.java | 여행 장소 엔티티다. |
| TripPlan.java | 여행 계획 엔티티로 대표 이미지와 장소 목록 연관관계를 가진다. |
| TripRepository.java | 여행 계획 저장소로 사용자별 카테고리/페이지네이션 조회를 담당한다. |
| TripRequest.java | 여행 계획 입력 DTO를 정의한다. |
| TripResponse.java | 여행 목록 페이지 DTO, 페이지 번호 DTO, 상세 DTO를 정의한다. |
| TripService.java | 여행 계획 비즈니스 로직과 목록용 D-Day/페이지네이션 DTO 조합을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 최근 페이지 템플릿 이름이 `trip-plan-*`에서 `trip-*`로 바뀌었으므로 경로와 파일명을 함께 유지한다.
- SSR 라우트는 `TripController` 기준으로 `/trip`, `/trip/create`, `/trip/detail`, `/trip/place`를 사용하고, `/trip` 목록은 `category`, `page` 쿼리 파라미터와 `sessionUser` 세션을 함께 사용한다.
- `TripController`의 목록 페이지는 세션 사용자 ID로 `TripService.getPlanList(...)`를 호출해 `tripPlans`, `pageDTO`, 카테고리 active 플래그를 Mustache에 내려준다.
- 현재 상세/장소추가 화면은 `planId` 없는 정적 라우트 구조이므로, 실제 데이터 연결 전까지는 템플릿과 컨트롤러의 URL 계약이 어긋나지 않게 유지하는 것이 중요하다.
- `TripApiController`의 목록 API도 `category`, `page`를 받지만 생성/목록/장소추가 API 전반은 아직 `userId=1` 고정 placeholder를 사용하므로 인증 연결 시 API와 화면 흐름을 같이 정리한다.
- `TripService.getPlanList(...)`는 `result`, `upcoming`, `past` 카테고리별 조회와 9개 단위 페이지네이션, D-Day 계산, 첫 장소명 노출을 담당하므로 목록 카드/페이저 변경 시 함께 확인한다.
- `TripPlan`은 `imgUrl`과 `places` 연관관계를 사용하고, 현재 목록 카드는 첫 번째 `TripPlace.placeName`과 대표 이미지에 의존하므로 seed 데이터와 null 방어를 같이 본다.
- 저장소는 `findPlanListByUserId`, `findUpcomingPlanListByUserId`, `findPastPlanListByUserId`와 각 count 쿼리까지 구현됐지만 생성/장소 추가/상세 조회 흐름은 여전히 TODO가 남아 있다.

## 테스트

- 목록 페이지의 카테고리 탭, 페이지네이션, 세션 사용자 기준 조회를 우선 확인한다.
- 생성, 상세, 장소 추가 API/화면 흐름은 placeholder/TODO 상태가 유지되는지도 함께 확인한다.

## 의존성

- 내부: `user`, `src/main/resources/templates/pages/trip-*`, `src/main/resources/db/data.sql`, `src/main/resources/db/mysql-init.sql`
- 외부: `Spring MVC`, `JPA/Hibernate`
