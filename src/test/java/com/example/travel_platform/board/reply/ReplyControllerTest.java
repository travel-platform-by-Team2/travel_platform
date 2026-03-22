package com.example.travel_platform.board.reply;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

class ReplyControllerTest {

    @Test
    void create() {
        ReplyService replyService = mock(ReplyService.class);
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(4, "cos", "cos@nate.com", "010-1234-5678", "USER"));
        ReplyController controller = new ReplyController(replyService, session);
        ReplyRequest.CreateDTO reqDTO = new ReplyRequest.CreateDTO();
        reqDTO.setContent("댓글");

        String view = controller.create(9, reqDTO);

        assertEquals("redirect:/boards/9", view);
        verify(replyService).createReply(4, 9, reqDTO);
    }

    @Test
    void del() {
        ReplyService replyService = mock(ReplyService.class);
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(4, "cos", "cos@nate.com", "010-1234-5678", "USER"));
        ReplyController controller = new ReplyController(replyService, session);

        String view = controller.delete(9, 3);

        assertEquals("redirect:/boards/9", view);
        verify(replyService).deleteReply(4, 9, 3);
    }
}
