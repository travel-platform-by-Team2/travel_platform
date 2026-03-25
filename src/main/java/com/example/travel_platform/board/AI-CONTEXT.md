<!-- Parent: ../AI-CONTEXT.md -->

# board

## 목적

커뮤니티 게시글 도메인의 SSR 화면, JSON API, 하위 `reply` / `like` 흐름을 함께 관리한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `Board.java` | 게시글 엔티티 |
| `BoardLike.java` | 게시글 좋아요 이력 엔티티 |
| `BoardController.java` | 목록/상세/작성/수정/삭제 SSR 요청 처리 |
| `BoardApiController.java` | 좋아요 토글 JSON 응답 처리 |
| `BoardRepository.java` | 게시글 저장, 단건 조회, 삭제 같은 기본 command repository |
| `BoardQueryRepository.java` | 목록, 검색, 통계, 집계성 조회를 담당하는 query repository |
| `BoardLikeRepository.java` | 좋아요 저장, 단건 조회, 집계, bulk 삭제를 담당하는 repository |
| `BoardRequest.java` | 게시글 입력 DTO |
| `BoardResponse.java` | 게시글 SSR/API 출력 DTO |
| `BoardService.java` | 게시글 도메인 규칙, 권한 검사, DTO 반환 처리 |

## 하위 디렉터리

- `reply/` - 댓글 하위 엔티티 도메인

## AI 작업 지침

- SSR 응답은 `BoardController`, JSON 응답은 `BoardApiController`에서 처리한다.
- SSR 루트 모델 규칙은 `model` / `models`를 사용한다.
- 목록 화면은 `BoardResponse.ListViewDTO`를 기준으로 `model`에는 화면 메타 정보, `models`에는 게시글 목록을 넣는다.
- 상세/작성/수정 화면은 `model` 단건 DTO만 사용한다.
- Entity를 직접 외부로 반환하지 않고 `BoardResponse` DTO로 변환해서 반환한다.
- `BoardResponse` 정적 팩토리 메서드는 `fromBoard`, `fromCreateRequest`, `createListView`처럼 역할이 드러나는 이름을 유지한다.
- `Board.category`는 `BoardCategory` enum을 사용하고, DB 저장은 `BoardCategoryConverter`가 문자열 코드로 처리한다.
- `BoardRepository`는 command 중심, `BoardQueryRepository`는 검색/통계/목록 중심으로 역할을 분리한다.
- 좋아요 bulk 삭제와 목록 좋아요 집계는 `BoardLikeRepository`가 담당한다.
- 목록/검색 query는 작성자 이름을 함께 쓰므로 `BoardQueryRepository`에서 `b.user`를 같이 조회한다.
- 목록 댓글 수는 `ReplyRepository.countByBoardIds(...)` 배치 집계로 가져와서 lazy collection 접근을 피한다.
- 좋아요 수 단일 기준은 `board_like_tb` 집계다. `Board` 엔티티에 별도 `likeCount` 컬럼을 두지 않는다.
- 좋아요 API는 `/api/boards/{boardId}/likes/toggle` 경로와 `Resp.ok(...)` 응답 패턴을 사용한다.
- 템플릿 계약이 바뀌면 `BoardControllerTest`, `BoardTemplateContractTest`를 함께 확인한다.

## 템플릿 계약

- `board-list.mustache`
  - `model.sortLabel`, `model.pageItems`, `model.prevPage`, `model.nextPage`
  - `models` 컬렉션으로 게시글 목록 렌더링
- `board-detail.mustache`
  - `model` 단건 DTO
  - `model.replies` 컬렉션을 포함한 상세 화면 DTO
- `board-create.mustache`, `board-edit.mustache`
  - `model` 단건 폼 DTO

## 테스트

- `BoardControllerTest`
  - SSR `model/models` 주입과 view path를 확인한다.
- `BoardApiControllerTest`
  - 좋아요 JSON 응답과 로그인 필요 예외를 확인한다.
- `BoardServiceTest`
  - 목록 검색, 상세 DTO, 권한 플래그, enum category 해석, 좋아요 토글 흐름을 확인한다.
- `BoardCategoryTest`
  - category 코드와 enum 매핑을 확인한다.
- `BoardTemplateContractTest`
  - `board-*` 템플릿이 `model/models` 계약을 따르는지 확인한다.

## 의존

- 도메인 `user`, `reply`
- 공통 `_core/handler/ex`, `_core/util`
- 프레임워크 `Spring MVC`, `JPA/Hibernate`, `Jakarta Validation`
