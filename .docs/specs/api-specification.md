# 여행 플랫폼 API 명세서 (최신)

- 문서 버전: `v2.0`
- 기준일: `2026-03-05`
- 기준 코드: `src/main/java/com/example/travel_platform`

## 1. 라우트 구분

- View 라우트: `@Controller`, Mustache 페이지 반환
- JSON API 라우트: `@RestController`, `/api/*` 경로 사용

## 2. View 라우트

| Method | URL | 설명 |
| --- | --- | --- |
| GET | `/` | 메인 페이지 진입 |
| GET | `/login-form` | 로그인 페이지 진입 |
| POST | `/login` | 로그인 처리(세션 저장 후 리다이렉트) |
| GET | `/join-form` | 회원가입 페이지 진입 |
| POST | `/join` | 회원가입 처리 |
| GET | `/logout` | 로그아웃 처리(세션 무효화) |
| GET | `/boards` | 게시글 목록 페이지 |
| GET | `/boards/new` | 게시글 작성 페이지 |
| POST | `/boards` | 게시글 작성 처리 |
| GET | `/boards/{boardId}` | 게시글 상세 페이지 |
| GET | `/boards/{boardId}/edit` | 게시글 수정 페이지 |
| POST | `/boards/{boardId}/update` | 게시글 수정 처리 |
| POST | `/boards/{boardId}/delete` | 게시글 삭제 처리 |
| POST | `/replies/boards/{boardId}` | 댓글 작성 처리 |
| POST | `/replies/{replyId}/update` | 댓글 수정 처리 (`boardId` 쿼리파라미터 필요) |
| POST | `/replies/{replyId}/delete` | 댓글 삭제 처리 (`boardId` 쿼리파라미터 필요) |
| GET | `/calendar` | 캘린더 페이지 진입 |
| GET | `/bookings/map-detail` | 숙소/지도 상세 페이지 |
| GET | `/bookings/checkout` | 예약 결제 페이지 |
| GET | `/bookings/complete` | 예약 완료 페이지 |

## 3. JSON API 라우트

| Method | URL | 설명 |
| --- | --- | --- |
| POST | `/api/trip-plans` | 여행 계획 생성 |
| GET | `/api/trip-plans` | 여행 계획 목록 조회 |
| GET | `/api/trip-plans/{planId}` | 여행 계획 상세 조회 |
| POST | `/api/trip-plans/{planId}/places` | 여행 장소 추가 |
| POST | `/api/bookings` | 예약 생성 |
| DELETE | `/api/bookings/{bookingId}` | 예약 취소 |
| GET | `/api/bookings` | 예약 목록 조회 |
| GET | `/api/bookings/{bookingId}` | 예약 상세 조회 |
| GET | `/api/bookings/place-image` | 숙소 대표 이미지 조회 |
| POST | `/api/bookings/map-pois/merge` | 지도 POI 병합 |
| GET | `/api/calendar` | 캘린더 조회 통합 API |
| POST | `/api/calendar` | 일정 생성 |
| PUT | `/api/calendar/{eventId}` | 일정 수정 |
| DELETE | `/api/calendar/{eventId}` | 일정 삭제 |
| POST | `/api/chatbot/messages` | 챗봇 질문/응답 |

## 4. `/api/calendar` 조회 파라미터 규칙

| 케이스 | 파라미터 | 반환 목적 |
| --- | --- | --- |
| 기간 조회 | `startDate`, `endDate` | 일정 목록 조회 |
| 월 노드 조회 | `year`, `month` | 월 단위 일자 노드 조회 |
| 단일 일자 조회 | `date` | 특정 일자 노드 조회 |

## 5. 이전 문서 대비 주요 변경점

- 커뮤니티 경로가 `community/*` 에서 `boards/*`, `replies/*` 로 변경됨
- 캘린더 JSON API가 `/calendar/events/*` 에서 `/api/calendar` 단일 경로 체계로 통합됨
- 예약 관련 JSON API는 `/bookings/*` 가 아니라 `/api/bookings/*` 사용
- 댓글 수정/삭제는 현재 `PUT/DELETE`가 아닌 `POST` 처리로 구현됨
