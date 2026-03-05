package com.example.travel_platform.board.reply;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final HttpSession session;

    @PostMapping("/boards/{boardId}")
    public String create(@PathVariable Integer boardId, ReplyRequest.CreateDTO reqDTO) {
        replyService.createReply(requireSessionUserId(), boardId, reqDTO);
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{replyId}/update")
    public String update(@PathVariable Integer replyId, @RequestParam Integer boardId, ReplyRequest.UpdateDTO reqDTO) {
        replyService.updateReply(requireSessionUserId(), replyId, reqDTO);
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{replyId}/delete")
    public String delete(@PathVariable Integer replyId, @RequestParam Integer boardId) {
        replyService.deleteReply(requireSessionUserId(), replyId);
        return "redirect:/boards/" + boardId;
    }

    private Integer requireSessionUserId() {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        return sessionUser.getId();
    }
}
