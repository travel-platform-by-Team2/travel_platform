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

    private static final int PAGE_SIZE = 10;
    private static final int PAGE_BLOCK_SIZE = 5;

    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createBoard(Integer sessionUserId, BoardRequest.CreateDTO reqDTO) {
        BoardActor actor = requireActor(sessionUserId);
        Board board = Board.create(actor.requireUser(), reqDTO.getTitle(), reqDTO.getCategory(), reqDTO.getContent());
        boardRepository.save(board);
    }

    @Transactional
    public void updateBoard(Integer sessionUserId, Integer boardId, BoardRequest.UpdateDTO reqDTO) {
        Board board = findBoard(boardId);
        BoardActor actor = requireActor(sessionUserId);
        validateBoardEditor(actor, board);
        board.update(reqDTO.getTitle(), reqDTO.getCategory(), reqDTO.getContent());
    }

    @Transactional
    public void deleteBoard(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);
        BoardActor actor = requireActor(sessionUserId);
        validateBoardEditor(actor, board);
        boardRepository.deleteLikesByBoard(boardId);
        boardRepository.delete(board);
    }

    public BoardResponse.ListPageDTO getBoardList(String category, String keyword, String sort, int page) {
        String normalizedSort = normalizeSort(sort);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<Board> boards = findBoardList(category, normalizedKeyword, normalizedSort, page);
        long totalCount = countBoardList(category, normalizedKeyword);
        List<BoardResponse.SummaryDTO> summaries = toSummaryDTOs(boards);
        int totalPages = resolveTotalPages(totalCount);
        List<BoardResponse.PageItemDTO> pageItems = createPageItems(page, totalPages);

        return BoardResponse.ListPageDTO.of(
                summaries,
                pageItems,
                page,
                PAGE_SIZE,
                totalCount,
                totalPages,
                category,
                normalizedKeyword,
                normalizedSort);
    }

    @Transactional
    public BoardResponse.DetailDTO getBoardDetail(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);
        BoardActor actor = resolveActorOrGuest(sessionUserId);

        increaseViewCount(board, actor);

        return BoardResponse.DetailDTO.of(
                board,
                findReplyItems(boardId, actor.userIdOrNull()),
                boardLikeRepository.countByBoard_Id(boardId),
                isLikedByMe(actor, boardId),
                actor.isOwner(board),
                actor.isAdmin());
    }

    public BoardResponse.FormDTO getBoardForm(Integer sessionUserId, Integer boardId) {
        Board board = findBoard(boardId);
        BoardActor actor = requireActor(sessionUserId);
        validateBoardEditor(actor, board);
        return BoardResponse.FormDTO.fromBoard(board);
    }

    @Transactional
    public BoardResponse.LikeToggleDTO toggleBoardLike(Integer sessionUserId, Integer boardId) {
        BoardActor actor = requireActor(sessionUserId);
        Board board = findBoard(boardId);
        validateLikePermission(actor, board);
        BoardLike boardLike = findBoardLikeOrNull(boardId, actor.userId());
        boolean liked = applyLikeToggle(board, actor, boardLike);

        return BoardResponse.LikeToggleDTO.of(liked, boardLikeRepository.countByBoard_Id(boardId));
    }

    private List<Board> findBoardList(String category, String keyword, String sort, int page) {
        int offset = page * PAGE_SIZE;

        if (hasKeyword(keyword)) {
            String[] words = toSearchWords(keyword);
            return boardRepository.search(normalizeCategory(category), words, sort, offset, PAGE_SIZE);
        }

        if (hasCategory(category)) {
            return boardRepository.findAllPagingByCategory(category, sort, offset, PAGE_SIZE);
        }

        return boardRepository.findAllPaging(sort, offset, PAGE_SIZE);
    }

    private long countBoardList(String category, String keyword) {
        if (hasKeyword(keyword)) {
            return boardRepository.countSearch(normalizeCategory(category), toSearchWords(keyword));
        }

        if (hasCategory(category)) {
            return boardRepository.countByCategory(category);
        }

        return boardRepository.count();
    }

    private List<BoardResponse.SummaryDTO> toSummaryDTOs(List<Board> boards) {
        Map<Integer, Long> likeCounts = resolveLikeCounts(boards);
        return boards.stream()
                .map(board -> BoardResponse.SummaryDTO.from(
                        board,
                        Math.toIntExact(likeCounts.getOrDefault(board.getId(), 0L))))
                .toList();
    }

    private Map<Integer, Long> resolveLikeCounts(List<Board> boards) {
        return boardRepository.countLikesByBoardIds(
                boards.stream().map(board -> board.getId()).toList());
    }

    private int resolveTotalPages(long totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        return totalPages == 0 ? 1 : totalPages;
    }

    private List<BoardResponse.PageItemDTO> createPageItems(int page, int totalPages) {
        int startPage = (page / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE;
        int endPage = startPage + PAGE_BLOCK_SIZE - 1;
        if (endPage >= totalPages) {
            endPage = totalPages - 1;
        }

        List<BoardResponse.PageItemDTO> pageItems = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageItems.add(BoardResponse.PageItemDTO.of(i, i == page));
        }
        return pageItems;
    }

    private void increaseViewCount(Board board, BoardActor actor) {
        if (!actor.isAdmin()) {
            board.increaseViewCount(actor.userIdOrNull());
        }
    }

    private List<BoardResponse.ReplyItemDTO> findReplyItems(Integer boardId, Integer sessionUserId) {
        return replyRepository.findByBoardId(boardId).stream()
                .map(reply -> BoardResponse.ReplyItemDTO.from(reply, sessionUserId))
                .toList();
    }

    private boolean isLikedByMe(BoardActor actor, Integer boardId) {
        return actor.hasUser() && boardLikeRepository.existsByBoard_IdAndUser_Id(boardId, actor.userId());
    }

    private void validateLikePermission(BoardActor actor, Board board) {
        if (actor.isOwner(board)) {
            throw new Exception403("본인 게시글에는 좋아요를 누를 수 없습니다.");
        }
    }

    private BoardLike findBoardLikeOrNull(Integer boardId, Integer sessionUserId) {
        return boardLikeRepository.findByBoard_IdAndUser_Id(boardId, sessionUserId).orElse(null);
    }

    private boolean applyLikeToggle(Board board, BoardActor actor, BoardLike boardLike) {
        if (boardLike != null) {
            boardLikeRepository.delete(boardLike);
            board.decreaseLikeCount();
            return false;
        }

        boardLikeRepository.save(BoardLike.create(board, actor.requireUser()));
        board.increaseLikeCount();
        return true;
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

    private String normalizeCategory(String category) {
        return hasCategory(category) ? category : null;
    }

    private boolean hasKeyword(String keyword) {
        return !keyword.isBlank();
    }

    private boolean hasCategory(String category) {
        return category != null && !category.isBlank();
    }

    private String[] toSearchWords(String keyword) {
        return keyword.split("\\s+");
    }

    private void validateBoardEditor(BoardActor actor, Board board) {
        if (!actor.canManage(board)) {
            throw new Exception403("본인 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    private BoardActor resolveActorOrGuest(Integer sessionUserId) {
        if (sessionUserId == null) {
            return BoardActor.guest();
        }
        return BoardActor.of(findUser(sessionUserId));
    }

    private BoardActor requireActor(Integer sessionUserId) {
        return BoardActor.of(findUser(sessionUserId));
    }

    private User findUser(Integer sessionUserId) {
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자 정보를 찾을 수 없습니다."));
    }

    private Board findBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
    }

    private static final class BoardActor {

        private final User user;

        private BoardActor(User user) {
            this.user = user;
        }

        private static BoardActor guest() {
            return new BoardActor(null);
        }

        private static BoardActor of(User user) {
            return new BoardActor(user);
        }

        private boolean hasUser() {
            return user != null;
        }

        private Integer userId() {
            return requireUser().getId();
        }

        private Integer userIdOrNull() {
            return hasUser() ? user.getId() : null;
        }

        private boolean isAdmin() {
            return hasUser() && user.isAdmin();
        }

        private boolean isOwner(Board board) {
            return hasUser() && board.getUser().getId().equals(user.getId());
        }

        private boolean canManage(Board board) {
            return isOwner(board) || isAdmin();
        }

        private User requireUser() {
            if (!hasUser()) {
                throw new IllegalStateException("로그인 사용자가 필요합니다.");
            }
            return user;
        }
    }
}
