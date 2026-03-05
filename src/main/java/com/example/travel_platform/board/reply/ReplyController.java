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
@RequestMapping("/boards/{boardId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final HttpSession session;

    @PostMapping
    public String create(@PathVariable("boardId") Integer boardId, ReplyRequest.CreateDTO reqDTO) {
        replyService.createReply(null, boardId, reqDTO);
        // replyService.createReply(requireSessionUserId(), boardId, reqDTO); TODO: 잠시
        // 비활성화해둠
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{replyId}/update")
    public String update(@PathVariable Integer replyId, @RequestParam Integer boardId, ReplyRequest.UpdateDTO reqDTO) {
        replyService.updateReply(null, replyId, reqDTO);
        // replyService.updateReply(requireSessionUserId(), replyId, reqDTO); TODO: 잠시
        // 비활성화해둠
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{replyId}/delete")
    public String delete(@PathVariable Integer replyId, @RequestParam Integer boardId) {
        replyService.deleteReply(null, replyId);
        // replyService.deleteReply(requireSessionUserId(), replyId); TODO: 잠시 비활성화해둠
        return "redirect:/boards/" + boardId;
    }

    // TODO: 잠시 비활성화 해둠
    // private Integer requireSessionUserId() {
    // User sessionUser = (User) session.getAttribute("sessionUser");
    // if (sessionUser == null) {
    // throw new Exception401("로그인이 필요합니다.");
    // }
    // return sessionUser.getId();
    // }
}
