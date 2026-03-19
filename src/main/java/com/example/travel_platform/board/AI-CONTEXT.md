<!-- Parent: ../AI-CONTEXT.md -->

리팩토링 완료

# board

## 목적

커뮤니티 게시글 도메인의 SSR 화면 흐름과 JSON API 흐름을 함께 다룬다.

## 주요 파일

| 파일명                     | 설명                                              |
| -------------------------- | ------------------------------------------------- |
| `Board.java`               | 게시글 엔티티                                     |
| `BoardLike.java`           | 게시글 좋아요 이력 엔티티                         |
| `BoardLikeRepository.java` | 게시글 좋아요 저장/조회용 JPA Repository          |
| `BoardController.java`     | 목록/상세/작성/수정/삭제 같은 SSR 화면 요청 처리  |
| `BoardApiController.java`  | 좋아요 토글 JSON 응답 처리                        |
| `BoardRepository.java`     | 게시글 조회/저장과 검색용 Repository              |
| `BoardRequest.java`        | 게시글 입력 DTO                                   |
| `BoardResponse.java`       | 게시글 화면용/응답용 DTO                          |
| `BoardService.java`        | 게시글 화면 DTO 조립, 권한 검사, 좋아요 토글 처리 |

## 하위 디렉토리

- `reply/` - 댓글 도메인

## AI 작업 지침

- SSR 응답은 `BoardController`, JSON 응답은 `BoardApiController`에서 처리한다.
- 게시글 목록 화면은 최상위 `model`, 작성/수정 화면은 최상위 `page` DTO 하나로 모델을 주입한다.
- 좋아요 API는 `/api/boards/{boardId}/likes/toggle` 경로와 `Resp.ok(...)` 응답 패턴을 사용한다.
- 좋아요 저장/조회는 `BoardLikeRepository`를 기준으로 다룬다.
- 게시글 검증 메시지와 Mustache 바인딩 이름은 템플릿과 반드시 함께 맞춘다.

## 테스트

- 목록, 상세, 작성, 수정, 삭제 SSR 흐름이 깨지지 않는지 확인한다.
- 상세 화면에서 좋아요 토글 JSON 응답이 프론트가 기대하는 `liked`, `likeCount` 필드를 유지하는지 확인한다.

## 의존

- 도메인: `user`, `reply`, `_core/handler/ex`, `_core/util`
- 프레임워크: `Spring MVC`, `JPA/Hibernate`, `Jakarta Validation`
