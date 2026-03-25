package com.example.travel_platform.board.reply;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.board.Board;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

class ReplyServiceTest {

    @Test
    void create() {
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReplyService replyService = new ReplyService(replyRepository, boardRepository, userRepository);

        User user = user(4, "cos", "USER");
        Board board = board(9, user(1, "author", "USER"));
        Reply reply = reply(3, board, user, "댓글");

        when(userRepository.findById(4)).thenReturn(Optional.of(user));
        when(boardRepository.findById(9)).thenReturn(Optional.of(board));
        when(replyRepository.save(org.mockito.ArgumentMatchers.any(Reply.class))).thenReturn(reply);

        ReplyResponse.CreatedDTO response = replyService.createReply(4, 9, createReq("댓글"));

        assertEquals(3, response.getId());
        assertEquals(9, response.getBoardId());
        assertEquals("cos", response.getUsername());
        assertTrue(response.isOwner());
    }

    @Test
    void upd() {
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReplyService replyService = new ReplyService(replyRepository, boardRepository, userRepository);

        User author = user(1, "author", "USER");
        User replyUser = user(4, "cos", "USER");
        Board board = board(9, author);
        Reply reply = reply(3, board, replyUser, "댓글");

        when(replyRepository.findById(3)).thenReturn(Optional.of(reply));
        when(userRepository.findById(4)).thenReturn(Optional.of(replyUser));

        ReplyResponse.UpdatedDTO response = replyService.updateReply(4, 9, 3, updateReq("수정 댓글"));

        assertEquals("수정 댓글", response.getContent());
        assertEquals("수정 댓글", reply.getContent());
    }

    @Test
    void mismatch() {
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReplyService replyService = new ReplyService(replyRepository, boardRepository, userRepository);

        User author = user(1, "author", "USER");
        User replyUser = user(4, "cos", "USER");
        Board board = board(9, author);
        Reply reply = reply(3, board, replyUser, "댓글");

        when(replyRepository.findById(3)).thenReturn(Optional.of(reply));

        Exception404 exception = assertThrows(Exception404.class, () -> replyService.updateReply(4, 7, 3, updateReq("수정 댓글")));

        assertEquals("게시글과 댓글 정보가 일치하지 않습니다.", exception.getMessage());
    }

    @Test
    void admin403() {
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReplyService replyService = new ReplyService(replyRepository, boardRepository, userRepository);

        User author = user(1, "author", "USER");
        User replyUser = user(4, "cos", "USER");
        User admin = user(9, "admin", "ADMIN");
        Board board = board(9, author);
        Reply reply = reply(3, board, replyUser, "댓글");

        when(replyRepository.findById(3)).thenReturn(Optional.of(reply));
        when(userRepository.findById(9)).thenReturn(Optional.of(admin));

        Exception403 exception = assertThrows(Exception403.class, () -> replyService.updateReply(9, 9, 3, updateReq("수정 댓글")));

        assertEquals("본인 댓글만 수정/삭제할 수 있습니다.", exception.getMessage());
    }

    @Test
    void del() {
        ReplyRepository replyRepository = mock(ReplyRepository.class);
        BoardRepository boardRepository = mock(BoardRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ReplyService replyService = new ReplyService(replyRepository, boardRepository, userRepository);

        User author = user(1, "author", "USER");
        User replyUser = user(4, "cos", "USER");
        Board board = board(9, author);
        Reply reply = reply(3, board, replyUser, "댓글");

        when(replyRepository.findById(3)).thenReturn(Optional.of(reply));
        when(userRepository.findById(4)).thenReturn(Optional.of(replyUser));

        replyService.deleteReply(4, 9, 3);

        verify(replyRepository).delete(reply);
    }

    private ReplyRequest.CreateDTO createReq(String content) {
        ReplyRequest.CreateDTO reqDTO = new ReplyRequest.CreateDTO();
        reqDTO.setContent(content);
        return reqDTO;
    }

    private ReplyRequest.UpdateDTO updateReq(String content) {
        ReplyRequest.UpdateDTO reqDTO = new ReplyRequest.UpdateDTO();
        reqDTO.setContent(content);
        return reqDTO;
    }

    private User user(int id, String username, String role) {
        User user = User.create(username, "1234", username + "@nate.com", "010-1111-2222", role);
        setField(user, "id", id);
        return user;
    }

    private Board board(int id, User author) {
        Board board = Board.create(author, "제목", "tips", "<p>본문</p>");
        setField(board, "id", id);
        return board;
    }

    private Reply reply(int id, Board board, User user, String content) {
        Reply reply = Reply.create(board, user, content);
        setField(reply, "id", id);
        setField(reply, "createdAt", LocalDateTime.of(2026, 3, 20, 13, 0));
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
