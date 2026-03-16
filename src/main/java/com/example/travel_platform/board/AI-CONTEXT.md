<!-- Parent: ../AI-CONTEXT.md -->

# board

## 목적

커뮤니티 게시판의 엔티티, 서비스, 저장소, MVC 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| Board.java | 게시글 엔티티다. |
| BoardLike.java | 게시글 좋아요 이력을 저장하는 엔티티다. |
| BoardController.java | 게시판 화면과 폼 요청을 처리한다. |
| BoardRepository.java | 게시글 저장/조회/삭제와 좋아요 토글용 native query를 담당한다. |
| BoardRequest.java | 게시글 입력 DTO를 정의한다. |
| BoardResponse.java | 게시글 응답 DTO를 정의한다. |
| BoardService.java | 게시글/댓글/좋아요 화면 DTO 조합과 권한 검사를 처리한다. |

## 하위 디렉토리

- `reply/` - 댓글 기능을 둔다.

## AI 작업 지침

- 게시글 검증 메시지와 model 속성명은 Mustache 템플릿과 함께 유지한다.
- 수정/삭제 권한 검사는 세션 사용자 기준으로 처리하므로 우회 로직을 추가하지 않는다.
- 좋아요 토글은 `board_like_tb` native query를 사용하므로, 연관 테이블명이나 컬럼명이 바뀌면 저장소와 서비스 설명을 함께 갱신한다.

## 테스트

- 목록, 상세, 작성, 수정, 삭제 흐름에서 세션 사용자 유무와 검증 실패 케이스를 함께 확인한다.
- 상세 화면의 좋아요 토글과 본인 게시글 좋아요 금지 규칙도 같이 확인한다.

## 의존성

- 내부: `user`, `reply`, `_core/handler/ex`
- 외부: `Spring MVC`, `JPA/Hibernate`, `Jakarta Validation`
