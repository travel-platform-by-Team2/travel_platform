package com.example.travel_platform.board.reply;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/boards/{boardId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private static final String REDIRECT_BOARD_PREFIX = "redirect:/boards/";

    private final ReplyService replyService;
    private final HttpSession session;

    @PostMapping("")
    public String create(@PathVariable(name = "boardId") Integer boardId, ReplyRequest.CreateDTO reqDTO) {
        replyService.createReply(requiredSessionUserId(), boardId, reqDTO);
        return redirectBoard(boardId);
    }

    @PostMapping("/{replyId}/delete")
    public String delete(@PathVariable(name = "boardId") Integer boardId,
            @PathVariable(name = "replyId") Integer replyId) {
        replyService.deleteReply(requiredSessionUserId(), boardId, replyId);
        return redirectBoard(boardId);
    }

    private String redirectBoard(Integer boardId) {
        return REDIRECT_BOARD_PREFIX + boardId;
    }

    private Integer requiredSessionUserId() {
        return SessionUsers.requireUserId(session);
    }
}
