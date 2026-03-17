<!-- Parent: ../AI-CONTEXT.md -->

# reply

## 목적

게시글 댓글 엔티티, 서비스, 저장소, MVC 요청 처리를 담당한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| Reply.java | 댓글 엔티티다. |
| ReplyController.java | 댓글 생성, 수정, 삭제 요청을 처리한다. |
| ReplyRepository.java | 댓글 저장과 조회를 담당한다. |
| ReplyRequest.java | 댓글 입력 DTO를 정의한다. |
| ReplyResponse.java | 댓글 AJAX 응답 DTO를 정의한다. |
| ReplyService.java | 댓글 비즈니스 로직을 처리한다. |

## 하위 디렉토리

- 없음

## AI 작업 지침

- 댓글은 게시글과 세션 사용자 컨텍스트를 함께 사용하므로 별도 기능처럼 분리하지 않는다.
- 게시글 상세 화면과 연결되는 model, redirect, JSON 응답 필드 이름을 유지한다.
- AJAX 응답은 `Map<String, Object>` 대신 `ReplyResponse` 내부 DTO로 정리한다.

## 테스트

- 댓글 생성, 수정, 삭제 후 게시글 상세 화면 흐름과 권한 검사를 확인한다.
- AJAX 생성, 수정 응답이 프론트에서 기대하는 키 이름과 일치하는지 확인한다.

## 의존

- 내부: `board`, `user`, `_core/handler/ex`
- 외부: `Spring MVC`, `JPA/Hibernate`
