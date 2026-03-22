package com.example.travel_platform.board.reply;

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

class ReplyApiControllerTest {

    @Test
    void create() {
        ReplyService replyService = mock(ReplyService.class);
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(5, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        ReplyApiController controller = new ReplyApiController(replyService, session);
        ReplyRequest.CreateDTO reqDTO = new ReplyRequest.CreateDTO();
        reqDTO.setContent("새 댓글");
        ReplyResponse.CreatedDTO responseDTO = ReplyResponse.CreatedDTO.builder()
                .id(1)
                .boardId(9)
                .username("ssar")
                .content("새 댓글")
                .createdAtDisplay("2026-03-20 12:00")
                .isOwner(true)
                .build();

        when(replyService.createReply(5, 9, reqDTO)).thenReturn(responseDTO);

        ResponseEntity<Resp<ReplyResponse.CreatedDTO>> response = controller.create(9, reqDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("새 댓글", response.getBody().getBody().getContent());
        verify(replyService).createReply(5, 9, reqDTO);
    }

    @Test
    void upd() {
        ReplyService replyService = mock(ReplyService.class);
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(5, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        ReplyApiController controller = new ReplyApiController(replyService, session);
        ReplyRequest.UpdateDTO reqDTO = new ReplyRequest.UpdateDTO();
        reqDTO.setContent("수정 댓글");
        ReplyResponse.UpdatedDTO responseDTO = ReplyResponse.UpdatedDTO.builder()
                .boardId(9)
                .replyId(3)
                .content("수정 댓글")
                .build();

        when(replyService.updateReply(5, 9, 3, reqDTO)).thenReturn(responseDTO);

        ResponseEntity<Resp<ReplyResponse.UpdatedDTO>> response = controller.update(9, 3, reqDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("수정 댓글", response.getBody().getBody().getContent());
        verify(replyService).updateReply(5, 9, 3, reqDTO);
    }

    @Test
    void api401() {
        ReplyService replyService = mock(ReplyService.class);
        ReplyApiController controller = new ReplyApiController(replyService, new MockHttpSession());
        ReplyRequest.UpdateDTO reqDTO = new ReplyRequest.UpdateDTO();
        reqDTO.setContent("수정 댓글");

        Exception401 exception = assertThrows(Exception401.class, () -> controller.update(9, 3, reqDTO));

        assertEquals("로그인이 필요합니다.", exception.getMessage());
    }
}
