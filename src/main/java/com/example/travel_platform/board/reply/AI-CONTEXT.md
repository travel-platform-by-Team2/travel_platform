<!-- Parent: ../AI-CONTEXT.md -->

# reply

## 목적

게시글 댓글 도메인의 SSR 흐름과 AJAX JSON 응답을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `Reply.java` | 댓글 엔티티 |
| `ReplyController.java` | 댓글 작성/삭제 SSR 요청 처리 |
| `ReplyApiController.java` | 댓글 생성/수정 JSON 응답 처리 |
| `ReplyRepository.java` | 댓글 저장 및 조회 처리 |
| `ReplyRequest.java` | 댓글 입력 DTO |
| `ReplyResponse.java` | 댓글 API 응답 DTO |
| `ReplyService.java` | 댓글 비즈니스 로직과 권한 검사 처리 |

## 하위 디렉토리

- 없음

## AI 작업 지침

- SSR redirect 흐름은 `ReplyController`, JSON 응답은 `ReplyApiController`에서 분리해 유지한다.
- 고정된 API 응답 스키마는 `ReplyResponse` DTO와 `Resp.ok(...)`를 사용한다.
- 댓글 API 경로는 `/api/boards/{boardId}/replies`, `/api/boards/{boardId}/replies/{replyId}`를 사용한다.
- 게시글 상세 Mustache와 연결된 필드 이름(`id`, `boardId`, `replyId`, `content`, `createdAtDisplay`, `username`, `isOwner`)은 프론트와 함께 맞춘다.

## 테스트

- 댓글 작성/삭제 SSR redirect 흐름이 유지되는지 확인한다.
- 댓글 API 생성/수정 응답이 프론트가 기대하는 필드 이름과 형식을 유지하는지 확인한다.

## 의존

- 도메인: `board`, `user`, `_core/handler/ex`, `_core/util`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`, `Jakarta Validation`
