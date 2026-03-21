package com.example.travel_platform.board;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform.board.reply.Reply;
import com.example.travel_platform.board.reply.ReplyRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

class BoardServiceTest {

    @Test
    void listSearch() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "ssar", "USER");
        Board board = board(10, author, "Busan route", "tips", "<p>Busan travel tips</p>");

        when(boardQueryRepository.search(eq("tips"), any(String[].class), eq("likes"), eq(10), eq(10)))
                .thenReturn(List.of(board));
        when(boardQueryRepository.countSearch(eq("tips"), any(String[].class))).thenReturn(12L);
        when(boardLikeRepository.countByBoardIds(List.of(10))).thenReturn(Map.of(10, 2L));

        BoardResponse.ListViewDTO response = boardService.getBoardList("tips", " busan trip ", "likes", 1);

        assertEquals("busan trip", response.getModel().getKeyword());
        assertEquals("likes", response.getModel().getSort());
        assertNotNull(response.getModel().getSortLabel());
        assertEquals(2, response.getModel().getTotalPages());
        assertEquals(2, response.getModel().getPageItems().size());
        assertTrue(response.getModel().isTips());
        assertTrue(response.getModel().isSortLikes());
        assertEquals(2, response.getModels().get(0).getLikeCount());

        ArgumentCaptor<String[]> wordsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(boardQueryRepository).search(eq("tips"), wordsCaptor.capture(), eq("likes"), eq(10), eq(10));
        assertArrayEquals(new String[] { "busan", "trip" }, wordsCaptor.getValue());
    }

    @Test
    void detailUser() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        User viewer = user(2, "viewer", "USER");
        Board board = board(7, author, "title", "tips", "<p>body</p>");
        Reply reply = reply(11, board, viewer, "reply");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));
        when(userRepository.findById(2)).thenReturn(Optional.of(viewer));
        when(replyRepository.findByBoardId(7)).thenReturn(List.of(reply));
        when(boardLikeRepository.existsByBoard_IdAndUser_Id(7, 2)).thenReturn(true);
        when(boardLikeRepository.countByBoard_Id(7)).thenReturn(5L);

        BoardResponse.DetailDTO response = boardService.getBoardDetail(2, 7);

        assertEquals(1, board.getViewCount());
        assertEquals(5L, response.getLikeCount());
        assertTrue(response.getLikedByMe());
        assertFalse(response.getIsOwner());
        assertFalse(response.getIsAdmin());
        assertEquals(1, response.getReplies().size());
        assertTrue(response.getReplies().get(0).isOwner());
    }

    @Test
    void detailGuest() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        Board board = board(7, author, "title", "tips", "<p>body</p>");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));
        when(replyRepository.findByBoardId(7)).thenReturn(List.of());
        when(boardLikeRepository.countByBoard_Id(7)).thenReturn(0L);

        BoardResponse.DetailDTO response = boardService.getBoardDetail(null, 7);

        assertEquals(1, board.getViewCount());
        assertFalse(response.getLikedByMe());
        assertFalse(response.getIsOwner());
        assertFalse(response.getIsAdmin());
    }

    @Test
    void detailOwner() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        Board board = board(7, author, "title", "tips", "<p>body</p>");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));
        when(userRepository.findById(1)).thenReturn(Optional.of(author));
        when(replyRepository.findByBoardId(7)).thenReturn(List.of());
        when(boardLikeRepository.existsByBoard_IdAndUser_Id(7, 1)).thenReturn(false);
        when(boardLikeRepository.countByBoard_Id(7)).thenReturn(0L);

        BoardResponse.DetailDTO response = boardService.getBoardDetail(1, 7);

        assertEquals(0, board.getViewCount());
        assertTrue(response.getIsOwner());
        assertFalse(response.getIsAdmin());
    }

    @Test
    void detailAdmin() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        User admin = user(9, "admin", "ADMIN");
        Board board = board(7, author, "title", "tips", "<p>body</p>");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));
        when(userRepository.findById(9)).thenReturn(Optional.of(admin));
        when(replyRepository.findByBoardId(7)).thenReturn(List.of());
        when(boardLikeRepository.existsByBoard_IdAndUser_Id(7, 9)).thenReturn(false);
        when(boardLikeRepository.countByBoard_Id(7)).thenReturn(0L);

        BoardResponse.DetailDTO response = boardService.getBoardDetail(9, 7);

        assertEquals(0, board.getViewCount());
        assertFalse(response.getIsOwner());
        assertTrue(response.getIsAdmin());
    }

    @Test
    void formAdmin() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        User admin = user(9, "admin", "ADMIN");
        Board board = board(7, author, "title", "tips", "<p>body</p>");

        when(boardRepository.findById(7)).thenReturn(Optional.of(board));
        when(userRepository.findById(9)).thenReturn(Optional.of(admin));

        BoardResponse.FormDTO response = boardService.getBoardForm(9, 7);

        assertEquals(7, response.getId());
        assertEquals("title", response.getTitle());
    }

    @Test
    void likeOn() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        User viewer = user(2, "viewer", "USER");
        Board board = board(9, author, "title", "tips", "<p>body</p>");

        when(boardRepository.findById(9)).thenReturn(Optional.of(board));
        when(boardLikeRepository.findByBoard_IdAndUser_Id(9, 2)).thenReturn(Optional.empty());
        when(userRepository.findById(2)).thenReturn(Optional.of(viewer));
        when(boardLikeRepository.countByBoard_Id(9)).thenReturn(1L);

        BoardResponse.LikeToggleDTO response = boardService.toggleBoardLike(2, 9);

        assertTrue(response.isLiked());
        assertEquals(1L, response.getLikeCount());
        assertEquals(1, board.getLikeCount());
        verify(boardLikeRepository).save(any(BoardLike.class));
    }

    @Test
    void likeSelf() {
        BoardRepository boardRepository = mock(BoardRepository.class);
        BoardQueryRepository boardQueryRepository = mock(BoardQueryRepository.class);
        BoardLikeRepository boardLikeRepository = mock(BoardLikeRepository.class);
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        BoardService boardService = new BoardService(
                boardRepository,
                boardQueryRepository,
                boardLikeRepository,
                replyRepository,
                userRepository);

        User author = user(1, "author", "USER");
        Board board = board(9, author, "title", "tips", "<p>body</p>");

        when(boardRepository.findById(9)).thenReturn(Optional.of(board));
        when(userRepository.findById(1)).thenReturn(Optional.of(author));

        assertThrows(Exception403.class, () -> boardService.toggleBoardLike(1, 9));
    }

    private User user(int id, String username, String role) {
        User user = User.create(username, "1234", username + "@nate.com", "010-1111-2222", role);
        setField(user, "id", id);
        return user;
    }

    private Board board(int id, User user, String title, String category, String content) {
        Board board = Board.create(user, title, category, content);
        setField(board, "id", id);
        setField(board, "createdAt", LocalDateTime.of(2026, 3, 20, 10, 0));
        return board;
    }

    private Reply reply(int id, Board board, User user, String content) {
        Reply reply = Reply.create(board, user, content);
        setField(reply, "id", id);
        setField(reply, "createdAt", LocalDateTime.of(2026, 3, 20, 11, 0));
        return reply;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
