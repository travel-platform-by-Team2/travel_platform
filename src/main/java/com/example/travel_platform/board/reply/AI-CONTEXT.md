<!-- Parent: ../AI-CONTEXT.md -->

# reply

## 목적

게시글 하위 댓글 도메인의 SSR redirect 흐름과 JSON API 흐름을 관리한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `Reply.java` | 댓글 엔티티 |
| `ReplyController.java` | 댓글 작성/삭제 SSR redirect 처리 |
| `ReplyApiController.java` | 댓글 작성/수정 JSON 응답 처리 |
| `ReplyRepository.java` | 댓글 저장과 게시글 기준 조회 처리 |
| `ReplyRequest.java` | 댓글 입력 DTO |
| `ReplyResponse.java` | 댓글 API 응답 DTO |
| `ReplyService.java` | 댓글 생성, 수정, 삭제와 owner-only 권한 처리 |

## AI 작업 지침

- SSR redirect 흐름은 `ReplyController`, JSON 응답은 `ReplyApiController`에서 처리한다.
- `ReplyController`는 `board-detail.mustache`의 작성/삭제 form과 직접 연결된다.
- `ReplyApiController`는 `/api/boards/{boardId}/replies`, `/api/boards/{boardId}/replies/{replyId}` 경로를 사용한다.
- 고정된 API 응답 스키마는 `ReplyResponse` DTO와 `Resp.ok(...)`로 반환한다.
- `ReplyResponse` 정적 팩토리 메서드는 `fromReply`처럼 변환 대상이 드러나는 이름을 유지한다.
- 상위 게시글의 좋아요 수와 카테고리 구조는 `BoardCategory` enum + `board_like_tb` 집계 기준을 따른다.
- 댓글 수정/삭제 권한은 게시글 owner/admin 정책과 다르게 댓글 작성자 본인만 허용한다.
- `ReplyService`는 `findEditableReply(...)` 기준으로 `조회 -> boardId 매칭 -> 권한 검증` 순서가 보이게 유지한다.

## 화면 계약

- 게시글 상세 화면은 `model.replies` 컬렉션으로 댓글 목록을 렌더링한다.
- 댓글 생성/수정 API 응답은 `id`, `boardId`, `username`, `content`, `createdAtDisplay`, `isOwner` 필드를 기준으로 프론트가 동작한다.

## 테스트

- `ReplyControllerTest`
  - 댓글 작성/삭제 SSR redirect를 확인한다.
- `ReplyApiControllerTest`
  - 댓글 생성/수정 JSON 응답과 로그인 필요 예외를 확인한다.
- `ReplyServiceTest`
  - 생성, 수정, 삭제, `boardId-replyId` 매칭, owner-only 권한을 확인한다.

## 의존

- 도메인 `board`, `user`
- 공통 `_core/handler/ex`, `_core/util`
- 프레임워크 `Spring MVC`, `JPA/Hibernate`, `Jakarta Validation`
