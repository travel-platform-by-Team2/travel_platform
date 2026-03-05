# 여행 플랫폼 테이블 명세서

- 문서 버전: `v1.0`
- 작성일: `2026-03-04`
- 기준 코드: `src/main/java/com/example/travel_platform` 하위 JPA 엔티티
- 적용 DB: `H2 (jdbc:h2:mem:test)`
- 스키마 생성 방식: `spring.jpa.hibernate.ddl-auto=create`

## 1. 문서 목적

본 문서는 현재 애플리케이션의 물리 테이블 구조를 정리한 기준 문서다.  
코드(JPA 엔티티)와 DB 스키마 간 정합성 확인, API/기능 개발 시 데이터 구조 참조를 목적으로 한다.

## 2. 테이블 목록

| 번호 | 테이블명 | 설명 |
| --- | --- | --- |
| 1 | `user_tb` | 유저 정보 테이블 |
| 2 | `trip_plan_tb` | 유저의 여행 계획(제목/기간) 테이블 |
| 3 | `trip_place_tb` | 여행 계획에 포함된 장소 정보 테이블 |
| 4 | `community_post_tb` | 커뮤니티 게시글 테이블 |
| 5 | `community_reply_tb` | 커뮤니티 게시글 댓글 테이블 |
| 6 | `booking_tb` | 숙소 예약 정보 테이블 |
| 7 | `calendar_event_tb` | 유저 일정(캘린더 이벤트) 테이블 |

## 3. 테이블 상세 명세

### 3.1 `user_tb - 유저 정보 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 사용자 PK |
| `username` | `VARCHAR(255)` | Y | N | Y | N | - | 로그인 아이디 |
| `password` | `VARCHAR(100)` | N | N | N | N | - | 비밀번호 |
| `email` | `VARCHAR(255)` | Y | N | N | N | - | 이메일 |
| `created_at` | `TIMESTAMP` | Y | N | N | N | 생성 시각 자동 입력 | 생성 시각 |

제약조건:
- PK: `id`
- UK: `username`

### 3.2 `trip_plan_tb - 유저의 여행 계획(제목/기간) 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 여행 계획 PK |
| `user_id` | `INTEGER` | N | N | N | Y | - | 소유 사용자 |
| `title` | `VARCHAR(100)` | N | N | N | N | - | 여행 계획 제목 |
| `start_date` | `DATE` | N | N | N | N | - | 시작일 |
| `end_date` | `DATE` | N | N | N | N | - | 종료일 |
| `created_at` | `TIMESTAMP` | Y | N | N | N | 생성 시각 자동 입력 | 생성 시각 |

제약조건:
- PK: `id`
- FK: `user_id -> user_tb.id`

### 3.3 `trip_place_tb - 여행 계획에 포함된 장소 정보 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 장소 PK |
| `trip_plan_id` | `INTEGER` | N | N | N | Y | - | 상위 여행 계획 |
| `place_name` | `VARCHAR(100)` | N | N | N | N | - | 장소명 |
| `address` | `VARCHAR(255)` | Y | N | N | N | - | 주소 |
| `latitude` | `NUMERIC(10,7)` | Y | N | N | N | - | 위도 |
| `longitude` | `NUMERIC(10,7)` | Y | N | N | N | - | 경도 |
| `day_order` | `INTEGER` | N | N | N | N | - | 일정 순서/일차 |

제약조건:
- PK: `id`
- FK: `trip_plan_id -> trip_plan_tb.id`

### 3.4 `community_post_tb - 커뮤니티 게시글 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 게시글 PK |
| `user_id` | `INTEGER` | N | N | N | Y | - | 작성자 |
| `title` | `VARCHAR(150)` | N | N | N | N | - | 게시글 제목 |
| `content` | `CLOB` | N | N | N | N | - | 게시글 본문 |
| `view_count` | `INTEGER` | N | N | N | N | `0` | 조회수 |
| `created_at` | `TIMESTAMP` | Y | N | N | N | 생성 시각 자동 입력 | 생성 시각 |

제약조건:
- PK: `id`
- FK: `user_id -> user_tb.id`

### 3.5 `community_reply_tb - 커뮤니티 게시글 댓글 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 댓글 PK |
| `post_id` | `INTEGER` | N | N | N | Y | - | 상위 게시글 |
| `user_id` | `INTEGER` | N | N | N | Y | - | 작성자 |
| `content` | `CLOB` | N | N | N | N | - | 댓글 본문 |
| `created_at` | `TIMESTAMP` | Y | N | N | N | 생성 시각 자동 입력 | 생성 시각 |

제약조건:
- PK: `id`
- FK: `post_id -> community_post_tb.id`
- FK: `user_id -> user_tb.id`

### 3.6 `booking_tb - 숙소 예약 정보 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 예약 PK |
| `user_id` | `INTEGER` | N | N | N | Y | - | 예약자 |
| `trip_plan_id` | `INTEGER` | N | N | N | Y | - | 연관 여행 계획 |
| `lodging_name` | `VARCHAR(120)` | N | N | N | N | - | 숙소명 |
| `check_in` | `DATE` | N | N | N | N | - | 체크인 일자 |
| `check_out` | `DATE` | N | N | N | N | - | 체크아웃 일자 |
| `guest_count` | `INTEGER` | N | N | N | N | - | 인원수 |
| `total_price` | `INTEGER` | N | N | N | N | - | 총 금액 |
| `created_at` | `TIMESTAMP` | Y | N | N | N | 생성 시각 자동 입력 | 생성 시각 |

제약조건:
- PK: `id`
- FK: `user_id -> user_tb.id`
- FK: `trip_plan_id -> trip_plan_tb.id`

### 3.7 `calendar_event_tb - 유저 일정(캘린더 이벤트) 테이블`

| 컬럼명 | 데이터 타입 | NULL | PK | UK | FK | 기본값 | 설명 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `id` | `INTEGER` | N | Y | N | N | auto increment | 일정 PK |
| `user_id` | `INTEGER` | N | N | N | Y | - | 일정 소유자 |
| `trip_plan_id` | `INTEGER` | Y | N | N | Y | - | 연관 여행 계획(선택) |
| `title` | `VARCHAR(120)` | N | N | N | N | - | 일정 제목 |
| `start_at` | `TIMESTAMP` | N | N | N | N | - | 시작 일시 |
| `end_at` | `TIMESTAMP` | N | N | N | N | - | 종료 일시 |
| `event_type` | `VARCHAR(50)` | N | N | N | N | - | 일정 유형 |

제약조건:
- PK: `id`
- FK: `user_id -> user_tb.id`
- FK: `trip_plan_id -> trip_plan_tb.id` (NULL 허용)

## 4. 테이블 관계 요약

| 부모 테이블 | 자식 테이블 | 관계 | FK 컬럼 |
| --- | --- | --- | --- |
| `user_tb` | `trip_plan_tb` | 1:N | `trip_plan_tb.user_id` |
| `trip_plan_tb` | `trip_place_tb` | 1:N | `trip_place_tb.trip_plan_id` |
| `user_tb` | `community_post_tb` | 1:N | `community_post_tb.user_id` |
| `community_post_tb` | `community_reply_tb` | 1:N | `community_reply_tb.post_id` |
| `user_tb` | `community_reply_tb` | 1:N | `community_reply_tb.user_id` |
| `user_tb` | `booking_tb` | 1:N | `booking_tb.user_id` |
| `trip_plan_tb` | `booking_tb` | 1:N | `booking_tb.trip_plan_id` |
| `user_tb` | `calendar_event_tb` | 1:N | `calendar_event_tb.user_id` |
| `trip_plan_tb` | `calendar_event_tb` | 1:N(선택) | `calendar_event_tb.trip_plan_id` |

## 5. 운영 메모

- 본 프로젝트는 JPA 엔티티 기준으로 테이블이 자동 생성된다.
- 명시적 인덱스는 별도 선언되어 있지 않으며, PK/UK에 따른 기본 인덱스만 기대할 수 있다.
- `calendar_event_tb.event_type`은 문자열 컬럼이며 enum/check 제약은 아직 없다.
- 다수 Repository/Service가 TODO 상태이므로 조회 정렬 기준, 삭제 정책(물리/논리), 중복 제약은 향후 확정이 필요하다.
