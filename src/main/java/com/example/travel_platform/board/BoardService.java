package com.example.travel_platform.board;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void updateBoard(User sessionUser, Integer boardId, BoardRequest.UpdateBoardDTO reqDTO) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        validateOwner(sessionUser, board);

        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
        board.setCategory(reqDTO.getCategory());
    }

    @Transactional
    public void deleteBoard(User sessionUser, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        validateOwner(sessionUser, board);
        boardRepository.deleteLikesByBoard(boardId);
        boardRepository.delete(board);
    }

    public BoardResponse.BoardListPageDTO getBoardList(String category, String keyword, String sort, int page) {
        int size = 10;
        int offset = page * size;
        String sortList = normalizeSort(sort);
        keyword = normalizeKeyword(keyword);
        boolean hasKeyword = !keyword.isBlank();

        List<Board> boards;
        long totalCount;

        if (hasKeyword) {
            String[] words = keyword.split("\\s+");
            if (category != null && !category.isBlank()) {
                boards = boardRepository.search(category, words, sortList, offset, size);
                totalCount = boardRepository.countSearch(category, words);
            } else {
                boards = boardRepository.search(null, words, sortList, offset, size);
                totalCount = boardRepository.countSearch(null, words);
            }
        } else if (category != null && !category.isBlank()) {
            boards = boardRepository.findAllPagingByCategory(category, sortList, offset, size);
            totalCount = boardRepository.countByCategory(category);
        } else {
            boards = boardRepository.findAllPaging(sortList, offset, size);
            totalCount = boardRepository.count();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Map<Integer, Long> likeCounts = boardRepository.countLikesByBoardIds(
                boards.stream().map(Board::getId).toList());

        List<BoardResponse.BoardSummaryDTO> boardDTOs = boards.stream()
                .map(board -> {
                    String plainText = Jsoup.parse(board.getContent()).text();
                    String summary = plainText.substring(0, Math.min(80, plainText.length()));
                    long likeCount = likeCounts.getOrDefault(board.getId(), 0L);

                    return BoardResponse.BoardSummaryDTO.builder()
                            .id(board.getId())
                            .title(board.getTitle())
                            .summary(summary)
                            .category(board.getCategory())
                            .categoryLabel(toCategoryLabel(board.getCategory()))
                            .categoryClass(toCategoryClass(board.getCategory()))
                            .username(board.getUser().getUsername())
                            .viewCount(board.getViewCount())
                            .likeCount(Math.toIntExact(likeCount))
                            .replyCount(board.getReplies().size())
                            .createdAtDisplay(board.getCreatedAt().format(formatter))
                            .build();
                })
                .toList();

        int totalPages = (int) Math.ceil((double) totalCount / size);
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
                .keyword(keyword)
                .sort(sortList)
                .sortLabel(toSortLabel(sortList))
                .isSortLikes("likes".equals(sortList))
                .isSortDownlikes("downlikes".equals(sortList))
                .isSortViews("view".equals(sortList))
                .isSortDownviews("downview".equals(sortList))
                .isSortLatest("latest".equals(sortList))
                .isSortDate("date".equals(sortList))
                .isTips("tips".equals(category))
                .isPlan("plan".equals(category))
                .isFood("food".equals(category))
                .isReview("review".equals(category))
                .isQna("qna".equals(category))
                .pageItems(pageItems)
                .build();
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "latest";
        }

        return switch (sort) {
            case "likes", "downlikes", "view", "downview", "latest", "date" -> sort;
            default -> "latest";
        };
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    private String toSortLabel(String sort) {
        return switch (sort) {
            case "likes" -> "좋아요순 ↑";
            case "downlikes" -> "좋아요순 ↓";
            case "view" -> "조회순 ↑";
            case "downview" -> "조회순 ↓";
            case "date" -> "날짜순 ↓";
            default -> "날짜순 ↑";
        };
    }

    @Transactional
    public BoardResponse.BoardDetailDTO getBoardDetail(Integer sessionUserId, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));

        List<BoardResponse.ReplyDTO> replies = board.getReplies().stream()
                .map(reply -> toReplyDTO(sessionUserId, reply))
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        boolean likedByMe = false;
        if (sessionUserId != null) {
            likedByMe = boardRepository.existsLike(boardId, sessionUserId);
        }
        long likeCount = boardRepository.countLike(boardId);
        board.setLikeCount(Math.toIntExact(likeCount));

        boolean isOwner = false;
        boolean isAdmin = false;

        if (sessionUserId != null) {
            isOwner = board.getUser().getId().equals(sessionUserId);
            User sessionUser = userRepository.findById(sessionUserId)
                    .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
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
        board.setLikeCount(Math.toIntExact(likeCount));

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
