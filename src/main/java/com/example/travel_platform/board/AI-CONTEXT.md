<!-- Parent: ../AI-CONTEXT.md -->

# board

## 목적

커뮤니티 게시글 도메인의 SSR 화면 흐름과 JSON API 흐름을 함께 다룬다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `Board.java` | 게시글 엔티티 |
| `BoardLike.java` | 게시글 좋아요 이력 엔티티 |
| `BoardController.java` | 목록/상세/작성/수정/삭제 같은 SSR 화면 요청 처리 |
| `BoardApiController.java` | 좋아요 토글 JSON 응답 처리 |
| `BoardRepository.java` | 게시글 조회/저장 및 좋아요 관련 native query 처리 |
| `BoardRequest.java` | 게시글 입력 DTO |
| `BoardResponse.java` | 게시글 화면용/응답용 DTO |
| `BoardService.java` | 게시글, 댓글, 좋아요 관련 화면 DTO 조립과 권한 검사 처리 |

## 하위 디렉토리

- `reply/` - 댓글 도메인

## AI 작업 지침

- SSR 응답은 `BoardController`, JSON 응답은 `BoardApiController`에서 처리한다.
- 게시글 목록 화면은 최상위 `model`, 작성/수정 화면은 최상위 `page` DTO 하나로 모델을 주입한다.
- 좋아요 토글 API는 기존 프론트 호환을 위해 현재 `/boards/{boardId}/likes/toggle` 경로를 유지한다. `/api` 접두사로 바꾸는 작업은 공개 API 경로 변경으로 간주하고 별도 합의 후 진행한다.
- 게시글 검증 메시지와 Mustache 바인딩 이름은 템플릿과 반드시 함께 맞춘다.
- 좋아요 집계는 `board_like_tb` native query를 사용하므로 테이블/컬럼 변경 시 repository와 service 설명을 함께 갱신한다.

## 테스트

- 목록, 상세, 작성, 수정, 삭제 SSR 흐름이 깨지지 않는지 확인한다.
- 상세 화면에서 좋아요 토글 JSON 응답이 기존 프론트가 기대하는 `liked`, `likeCount` 필드를 유지하는지 확인한다.

## 의존

- 도메인: `user`, `reply`, `_core/handler/ex`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`, `Jakarta Validation`
