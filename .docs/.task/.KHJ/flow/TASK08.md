# TASK08 - community 도메인 board 전환 및 캘린더 뷰 컨트롤러 추가

## 0. 작업 유형

- 리팩토링

## 1. 작업 목표

- `community` 도메인을 `board` 도메인으로 전환한다.
- 게시글/댓글 구조를 분리해 댓글 도메인을 `board/reply` 하위로 배치한다.
- `board`는 뷰 응답용 `@Controller` 중심으로 구성하고 `RestController`는 제거한다.
- 캘린더 페이지 응답용 `@Controller`를 추가한다.

## 2. 작업 범위

- `community` 패키지 파일을 `board` 패키지로 전환
- 게시글 엔티티: `board` 루트 유지
- 댓글 엔티티: `board/reply` 폴더로 이동
- `CommunityApiController` 제거 및 `BoardController` 추가(`@Controller`)
- `CalendarController` 추가(뷰 응답)
- 관련 참조(import, 타입명, SQL 테이블명 참조) 동기화
- 문서/작업 로그 업데이트

## 3. 작업 제외 범위

- 비즈니스 로직 구현(TODO 해소)
- 인증/권한 정책 변경
- DB 마이그레이션 스크립트 작성
- 프론트 템플릿 파일명 전면 변경

## 4. 완료 기준

- [x] `community` 도메인이 `board` 도메인으로 전환된다.
- [x] 댓글 도메인이 `board/reply` 하위로 분리된다.
- [x] `BoardController`가 뷰 응답 전용 `@Controller`로 동작한다.
- [x] 캘린더 뷰 응답용 `CalendarController`가 존재한다.
- [x] `./gradlew compileJava` 성공

## 5. 예상 영향 범위

- 대상 레이어: board domain, calendar controller, chatbot query reference
- 대상 기능: 게시판/댓글 도메인 구조, 캘린더 페이지 라우팅
- 사용자 영향: 라우팅/도메인 명칭 정리, 구조 가독성 향상

## 6. 위험도

- 등급: MEDIUM
- 근거: 패키지/클래스 리네이밍 및 다중 파일 참조 변경

## 7. 승인 필요 여부

- 필요 여부: 예(완료)
- 승인 필요 사유: 파일 다수 변경 + 도메인 리네이밍

## 8. 작업 Workflow (필수)

### 8.1 단계 정의

| 단계 | 목표 | 입력 | 출력 | 검증 |
|---|---|---|---|---|
| 1 | board 도메인 파일 구성 | 기존 community 파일 | board 패키지 클래스 | 컴파일 |
| 2 | reply 하위 폴더 분리 | 댓글 엔티티 | `board/reply` 구조 | import 점검 |
| 3 | 컨트롤러 전환 | CommunityApiController/CalendarApiController | BoardController + CalendarController | 라우팅 점검 |
| 4 | 참조 동기화/검증 | chatbot/sql 참조 포함 | 컴파일 가능한 코드 | `./gradlew compileJava` |

### 8.2 중단 조건

- 범위 외 API 스펙 변경 요구 발생 시
- DB/설정 변경이 필수로 요구될 시

## 9. 검증 계획

- 빌드/컴파일: `./gradlew compileJava`

## 10. 결과 기록(작업 후 작성)

- 변경 파일:
  - `src/main/java/com/example/travel_platform/board/Board.java` (추가)
  - `src/main/java/com/example/travel_platform/board/reply/BoardReply.java` (추가)
  - `src/main/java/com/example/travel_platform/board/BoardRequest.java` (추가)
  - `src/main/java/com/example/travel_platform/board/BoardResponse.java` (추가)
  - `src/main/java/com/example/travel_platform/board/BoardRepository.java` (추가)
  - `src/main/java/com/example/travel_platform/board/BoardService.java` (추가)
  - `src/main/java/com/example/travel_platform/board/BoardController.java` (추가)
  - `src/main/java/com/example/travel_platform/calendar/CalendarController.java` (추가)
  - `src/main/java/com/example/travel_platform/community/*` (삭제)
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java` (board 테이블명 참조로 수정)
  - `.docs/.task/.KHJ/flow/TASK08.md` (결과 기록)
  - `.docs/.task/.KHJ/WORKLOG.md` (이력 기록)
- 테스트 결과:
  - `./gradlew compileJava` 성공
  - `./gradlew test --tests "*ChatbotServiceTest"` 성공
  - `./gradlew test --tests "*ChatbotControllerTest"` 성공
- 미완료 항목(TODO):
  - Board 페이지 템플릿 파일명(`community-*.mustache`) 일괄 리네이밍 여부 결정
  - Board 도메인 쓰기/조회 실제 구현(TODO 로직) 반영
- 리스크/주의사항:
  - 엔티티 테이블명이 `board_tb`, `board_reply_tb`로 변경되어 DB 스키마 동기화가 필요할 수 있음
