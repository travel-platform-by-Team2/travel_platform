package com.example.travel_platform.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class BoardApiControllerTest {

    @Test
    void likeOk() {
        BoardService boardService = mock(BoardService.class);
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(5, "cos", "cos@nate.com", "010-1234-5678", "USER"));
        BoardApiController controller = new BoardApiController(boardService, session);
        BoardResponse.LikeToggleDTO responseDTO = BoardResponse.LikeToggleDTO.createLikeToggle(true, 3);

        when(boardService.toggleBoardLike(5, 9)).thenReturn(responseDTO);

        ResponseEntity<Resp<BoardResponse.LikeToggleDTO>> response = controller.toggleLike(9);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("성공", response.getBody().getMsg());
        assertEquals(true, response.getBody().getBody().isLiked());
        assertEquals(3, response.getBody().getBody().getLikeCount());
        verify(boardService).toggleBoardLike(5, 9);
    }

    @Test
    void like401() {
        BoardService boardService = mock(BoardService.class);
        BoardApiController controller = new BoardApiController(boardService, new MockHttpSession());

        Exception401 exception = assertThrows(Exception401.class, () -> controller.toggleLike(9));

        assertEquals("로그인이 필요합니다.", exception.getMessage());
    }
}
