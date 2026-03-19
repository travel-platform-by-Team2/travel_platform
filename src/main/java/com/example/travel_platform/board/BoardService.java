package com.example.travel_platform.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.reply.ReplyRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createBoard(Integer sessionUserId, BoardRequest.CreateDTO reqDTO) {
        User sessionUser = findUser(sessionUserId);
        Board board = Board.create(sessionUser, reqDTO.getTitle(), reqDTO.getCategory(), reqDTO.getContent());
        boardRepository.save(board);
    }

    @Transactional
    public void updateBoard(Integer sessionUserId, Integer boardId, BoardRequest.UpdateDTO reqDTO) {
        Board board = findBoard(boardId);
        validateOwner(sessionUserId, board);
        board.update(reqDTO.getTitle(), reqDTO.getCategory(), reqDTO.getContent());
    }

    @Transactional
    public void deleteBoard(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);
        validateOwner(sessionUserId, board);
        boardRepository.deleteLikesByBoard(boardId);
        boardRepository.delete(board);
    }

    public BoardResponse.ListPageDTO getBoardList(String category, String keyword, String sort, int page) {
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

        Map<Integer, Long> likeCounts = boardRepository.countLikesByBoardIds(
                boards.stream().map(Board::getId).toList());

        List<BoardResponse.SummaryDTO> boardDTOs = boards.stream()
                .map(board -> BoardResponse.SummaryDTO.from(
                        board,
                        Math.toIntExact(likeCounts.getOrDefault(board.getId(), 0L))))
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

        return BoardResponse.ListPageDTO.builder()
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

    @Transactional
    public BoardResponse.DetailDTO getBoardDetail(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);
        boolean isAdmin = sessionUserId != null && findUser(sessionUserId).isAdmin();

        if (!isAdmin) {
            board.increaseViewCount(sessionUserId);
        }

        List<BoardResponse.ReplyItemDTO> replies = replyRepository.findByBoardId(boardId).stream()
                .map(reply -> BoardResponse.ReplyItemDTO.from(reply, sessionUserId))
                .toList();

        boolean likedByMe = sessionUserId != null && boardLikeRepository.existsByBoard_IdAndUser_Id(boardId, sessionUserId);
        long likeCount = boardLikeRepository.countByBoard_Id(boardId);
        boolean isOwner = sessionUserId != null && board.getUser().getId().equals(sessionUserId);

        return BoardResponse.DetailDTO.of(board, replies, likeCount, likedByMe, isOwner, isAdmin);
    }

    public BoardResponse.FormDTO getBoardForm(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);
        validateOwner(sessionUserId, board);
        return BoardResponse.FormDTO.fromBoard(board);
    }

    @Transactional
    public BoardResponse.LikeToggleDTO toggleBoardLike(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);

        if (board.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("본인 게시글에는 좋아요를 누를 수 없습니다.");
        }

        User sessionUser = findUser(sessionUserId);
        BoardLike boardLike = boardLikeRepository.findByBoard_IdAndUser_Id(boardId, sessionUserId).orElse(null);
        boolean liked;

        if (boardLike != null) {
            boardLikeRepository.delete(boardLike);
            board.decreaseLikeCount();
            liked = false;
        } else {
            boardLikeRepository.save(BoardLike.create(board, sessionUser));
            board.increaseLikeCount();
            liked = true;
        }

        long likeCount = boardLikeRepository.countByBoard_Id(boardId);
        return BoardResponse.LikeToggleDTO.of(liked, likeCount);
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

    private void validateOwner(Integer sessionUserId, Board board) {
        User sessionUser = findUser(sessionUserId);
        boolean isOwner = board.getUser().getId().equals(sessionUser.getId());
        boolean isAdmin = sessionUser.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new Exception403("본인 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    private User findUser(Integer sessionUserId) {
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));
    }

    private Board findBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
    }
}
