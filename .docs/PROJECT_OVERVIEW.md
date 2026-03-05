# 여행 플랫폼 프로젝트 개요 (Project Overview)

- **작성일**: 2026-03-05
- **버전**: v1.0.0
- **문서 목적**: 프로젝트의 전체적인 구조, 기술 스택, 데이터 모델 및 현재 개발 현황을 요약 설명합니다.

---

## 1. 프로젝트 요약
본 프로젝트는 사용자가 여행 계획을 수립하고, 장소를 관리하며, 숙소 예약 및 커뮤니티 활동을 할 수 있는 종합 여행 관리 플랫폼입니다. 개인화된 캘린더 기능을 통해 여행 일정과 개인 일정을 통합 관리하는 기능을 제공합니다.

---

## 2. 기술 스택 (Tech Stack)

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 4.0.3
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security (인증/권한 관리 예정)
- **Template Engine**: Mustache

### Database
- **Main DB**: MySQL (Production), H2 (Development/Test)
- **Init Scripts**: `data.sql`, `mapdata.sql`, `mysql-init.sql`

### Frontend
- **Library**: jQuery, Bootstrap 5
- **Editor**: Summernote (게시글 작성용)
- **Map API**: (구현 예정) 위도/경도 기반 장소 관리

---

## 3. 데이터 모델 (Domain Entities)

| 엔티티 | 설명 | 주요 속성 |
| :--- | :--- | :--- |
| **User** | 사용자 정보 | email(ID), username, password |
| **TripPlan** | 여행 계획 메인 | title, start_date, end_date |
| **TripPlace** | 여행 상세 장소 | place_name, address, latitude, longitude |
| **CommunityPost** | 커뮤니티 게시글 | title, content, view_count |
| **Booking** | 숙소 예약 정보 | lodging_name, check_in, check_out, total_price |
| **CalendarEvent** | 일정 정보 | title, start_at, end_at, event_type |

---

## 4. 핵심 기능 (Core Features)

### 4.1 여행 계획 및 장소 관리
- 사용자는 여행 일정을 생성하고 관리할 수 있습니다.
- 각 여행 일정 내에 방문할 장소들을 순서대로 배치하고 지도 좌표 정보를 저장합니다.

### 4.2 커뮤니티 및 댓글
- 여행 정보를 공유할 수 있는 게시판 기능을 제공합니다.
- Summernote 에디터를 활용하여 풍부한 텍스트 편집이 가능합니다.
- 게시글에 대한 댓글 작성을 통해 사용자 간 소통을 지원합니다.

### 4.3 예약 및 캘린더 연동
- 여행 계획과 연동된 숙소 예약 기능을 제공합니다.
- 확정된 여행 일정과 예약 정보는 캘린더 서비스와 동기화되어 시각적으로 관리됩니다.

---

## 5. 현재 개발 현황 (Current Status)

### 완료된 작업
- 기본 도메인 엔티티 및 Repository 설계 완료
- 이메일 기반 로그인/회원가입 로직 구현 (User)
- Mustache 기반의 페이지 템플릿 구조화
- 기본적인 데이터베이스 초기화 스크립트 작성

### 진행 중/예정 작업
- 여행 계획 및 장소 추가의 서비스 로직 구체화
- 지도 API 연동을 통한 장소 시각화
- 예약 시스템의 금액 계산 및 유효성 검증 로직
- 캘린더 일정 드래그 앤 드롭 등 인터랙티브 기능 강화
- Spring Security를 활용한 세밀한 권한 제어

---

## 6. 프로젝트 구조 (Directory Structure)
- `src/main/java/com/example/travel_platform`: 백엔드 소스 코드
- `src/main/resources/templates`: Mustache 페이지 템플릿
- `src/main/resources/static`: JS, CSS, 외부 라이브러리(Summernote 등)
- `.docs/specs`: 상세 설계 문서 및 API 명세서
