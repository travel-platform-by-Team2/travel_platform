package com.example.travel_platform.board;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.reply.Reply;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createBoard(Integer sessionUserId, BoardRequest.CreateBoardDTO reqDTO) {

        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));

        Board board = new Board();
        board.setUser(sessionUser);
        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
        board.setViewCount(0);
        board.setCategory(reqDTO.getCategory());

        boardRepository.save(board);
    }

    @Transactional
    public void updateBoard(User sessionUserId, Integer boardId, BoardRequest.UpdateBoardDTO reqDTO) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        validateOwner(sessionUserId, board);

        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
        board.setCategory(reqDTO.getCategory());
    }

    @Transactional
    public void deleteBoard(User sessionUserId, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        validateOwner(sessionUserId, board);
        boardRepository.deleteLikesByBoard(boardId); // 좋아요 먼저 삭제
        boardRepository.delete(board); // 글삭제
    }

    public BoardResponse.BoardListPageDTO getBoardList(String category, int page) {
        int size = 10;
        int offset = page * size;

        List<Board> boards;
        long totalCount;

        if (category != null && !category.isBlank()) {
            boards = boardRepository.findAllPagingByCategory(category, offset, size);
            totalCount = boardRepository.countByCategory(category);
        } else {
            boards = boardRepository.findAllPaging(offset, size);
            totalCount = boardRepository.count();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<BoardResponse.BoardSummaryDTO> boardDTOs = boards.stream()
                .map(board -> {
                    String plainText = Jsoup.parse(board.getContent()).text();
                    String summary = plainText.substring(0, Math.min(80, plainText.length()));

                    return BoardResponse.BoardSummaryDTO.builder()
                            .id(board.getId())
                            .title(board.getTitle())
                            .summary(summary)
                            .category(board.getCategory())
                            .categoryLabel(toCategoryLabel(board.getCategory()))
                            .categoryClass(toCategoryClass(board.getCategory()))
                            .username(board.getUser().getUsername())
                            .viewCount(board.getViewCount())
                            .replyCount(board.getReplies().size())
                            .createdAtDisplay(board.getCreatedAt().format(formatter))
                            .build();
                })
                .toList();

        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 게시글이 하나도 없을 때 page=0 기준 유지
        if (totalPages == 0) {
            totalPages = 1;
        }

        boolean first = page == 0;
        boolean last = page >= totalPages - 1;

        Integer prevPage = first ? null : page - 1;
        Integer nextPage = last ? null : page + 1;

        int blockSize = 5;
        int startPage = (page / blockSize) * blockSize;
        int endPage = startPage + blockSize - 1;

        if (endPage >= totalPages) {
            endPage = totalPages - 1;
        }

        List<BoardResponse.PageItemDTO> pageItems = new ArrayList<>();

        for (int i = startPage; i <= endPage; i++) {
            pageItems.add(BoardResponse.PageItemDTO.builder()
                    .page(i)
                    .displayNumber(i + 1)
                    .current(i == page)
                    .build());
        }

        return BoardResponse.BoardListPageDTO.builder()
                .boards(boardDTOs)
                .currentPage(page)
                .pageNumber(page + 1)
                .size(size)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .first(first)
                .last(last)
                .prevPage(prevPage)
                .nextPage(nextPage)
                .category(category)
                .isTips("tips".equals(category))
                .isPlan("plan".equals(category))
                .isFood("food".equals(category))
                .isReview("review".equals(category))
                .isQna("qna".equals(category))
                .pageItems(pageItems)
                .build();
    }

    @Transactional
    public BoardResponse.BoardDetailDTO getBoardDetail(Integer sessionUserId, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        List<BoardResponse.ReplyDTO> replies = board.getReplies().stream()
                .map(reply -> toReplyDTO(sessionUserId, reply))
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 좋아요 계산
        boolean likedByMe = false;
        if (sessionUserId != null) {
            likedByMe = boardRepository.existsLike(boardId, sessionUserId);
        }
        long likeCount = boardRepository.countLike(boardId);

        boolean isOwner = false;
        boolean isAdmin = false;

        if (sessionUserId != null) {
            isOwner = board.getUser().getId().equals(sessionUserId);
            User sessionUser = userRepository.findById(sessionUserId)
                    .orElseThrow(() -> new Exception404("사용자를 찾을 수가 없습니다."));
            isAdmin = sessionUser.isAdmin();
        }

        if (!isAdmin) {
            board.increaseViewCount(sessionUserId);
        }

        return BoardResponse.BoardDetailDTO.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .category(board.getCategory())
                .categoryLabel(toCategoryLabel(board.getCategory()))
                .categoryClass(toCategoryClass(board.getCategory()))
                .username(board.getUser().getUsername())
                .viewCount(board.getViewCount())
                .replyCount(board.getReplies().size())
                .createdAtDisplay(board.getCreatedAt().format(formatter))
                .replies(replies)
                .isOwner(isOwner)
                .isAdmin(isAdmin)
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .build();
    }

    // 화면에 보여주는 한글 텍스트 변환
    private String toCategoryLabel(String category) {
        if (category == null || category.isBlank()) {
            return "기타";
        }
        return switch (category) {
            case "tips" -> "여행 팁";
            case "plan" -> "여행 계획";
            case "food" -> "맛집/카페";
            case "review" -> "숙소 후기";
            case "qna" -> "질문/답변";
            default -> "기타";
        };
    }

    // css 변환 파일
    private String toCategoryClass(String category) {
        if (category == null || category.isBlank()) {
            return "cat-default";
        }
        return switch (category) {
            case "tips" -> "cat-tips";
            case "plan" -> "cat-plan";
            case "food" -> "cat-food";
            case "review" -> "cat-review";
            case "qna" -> "cat-qna";
            default -> "cat-default";
        };
    }

    private BoardResponse.ReplyDTO toReplyDTO(Integer sessionUserId, Reply reply) {
        boolean isOwner = false;
        if (sessionUserId != null) {
            isOwner = reply.getUser().getId().equals(sessionUserId);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return BoardResponse.ReplyDTO.builder()
                .id(reply.getId())
                .boardId(reply.getBoard().getId())
                .username(reply.getUser().getUsername())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .createdAtDisplay(reply.getCreatedAt().format(formatter))
                .isOwner(isOwner)
                .build();
    }

    // 좋아요 버튼을 누르면 응답을 즉시 주기위해 비즈니스 규칙을 한곳에 모은곳
    @Transactional
    public BoardResponse.ToggleLikeDTO toggleBoardLike(Integer sessionUserId, Integer boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        if (board.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("본인 게시글에는 좋아요를 누를 수 없습니다.");
        }

        userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));

        boolean liked = boardRepository.existsLike(boardId, sessionUserId);

        if (liked) {
            boardRepository.deleteLike(boardId, sessionUserId);
            liked = false;
        } else {
            boardRepository.insertLike(boardId, sessionUserId);
            liked = true;
        }

        long likeCount = boardRepository.countLike(boardId);

        return BoardResponse.ToggleLikeDTO.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }

    private void validateOwner(User sessionUser, Board board) {
        boolean isOwner = board.getUser().getId().equals(sessionUser.getId());
        boolean isAdmin = sessionUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new Exception403("본인 게시글만 수정/삭제할 수 있습니다.");
        }
    }
}
