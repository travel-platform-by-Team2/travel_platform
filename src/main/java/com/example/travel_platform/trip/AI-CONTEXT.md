<!-- Parent: ../AI-CONTEXT.md -->

# trip

## 목적

여행 계획 생성, 목록, 상세, 장소 추가 기능의 화면/API/도메인을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| TripApiController.java | 여행 계획 관련 REST API를 제공한다. |
| TripController.java | 여행 계획 화면 라우팅을 담당한다. |
| TripPlace.java | 여행 장소 엔티티다. |
| TripPlan.java | 여행 계획 엔티티다. |
| TripRepository.java | 여행 계획 저장소다. |
| TripRequest.java | 여행 계획 입력 DTO를 정의한다. |
| TripResponse.java | 여행 계획 응답 DTO를 정의한다. |
| TripService.java | 여행 계획 비즈니스 로직을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 최근 페이지 템플릿 이름이 `trip-plan-*`에서 `trip-*`로 바뀌었으므로 경로와 파일명을 함께 유지한다.
- SSR 라우트는 `TripController` 기준으로 `/trip`, `/trip/create`, `/trip/detail`, `/trip/place`를 사용하므로 템플릿 링크와 헤더 활성화 경로도 같은 기준으로 맞춘다.
- 현재 상세/장소추가 화면은 `planId` 없는 정적 라우트 구조이므로, 실제 데이터 연결 전까지는 템플릿과 컨트롤러의 URL 계약이 어긋나지 않게 유지하는 것이 중요하다.
- 저장소 구현과 일부 서비스 로직에 TODO가 남아 있으므로 실제 영속성 추가 시 API와 화면 흐름을 같이 보강한다.

## 테스트

- 목록, 생성, 상세, 장소 추가 API/화면 흐름을 함께 확인한다.

## 의존성

- 내부: `src/main/resources/templates/pages/trip-*`
- 외부: `Spring MVC`, `JPA/Hibernate`
