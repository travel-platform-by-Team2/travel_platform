<!-- Parent: ../AI-CONTEXT.md -->

# reply

## 목적

게시글 댓글의 엔티티, 서비스, 저장소, MVC 흐름을 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| Reply.java | 댓글 엔티티다. |
| ReplyController.java | 댓글 작성/삭제 요청을 처리한다. |
| ReplyRepository.java | 댓글 저장과 조회를 담당한다. |
| ReplyRequest.java | 댓글 입력 DTO를 정의한다. |
| ReplyService.java | 댓글 비즈니스 로직을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 댓글은 게시글과 세션 사용자 컨텍스트에 의존하므로 단독 기능처럼 분리하지 않는다.
- 게시글 상세 화면과 연결되는 model/redirect 흐름을 유지한다.

## 테스트

- 댓글 작성/삭제 후 게시글 상세 화면으로의 흐름과 권한 검사를 확인한다.

## 의존성

- 내부: `board`, `user`, `_core/handler/ex`
- 외부: `Spring MVC`, `JPA/Hibernate`
