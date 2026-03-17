package com.example.travel_platform.board.reply;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @PostMapping("")
    public String create(@PathVariable("boardId") Integer boardId, ReplyRequest.CreateDTO reqDTO) {
        replyService.createReply(requireSessionUserId(), boardId, reqDTO);
        return "redirect:/boards/" + boardId;
    }

    @ResponseBody
    @PostMapping("/ajax")
    public ReplyResponse.CreateAjaxDTO createAjax(@PathVariable("boardId") Integer boardId,
            ReplyRequest.CreateDTO reqDTO) {
        Reply reply = replyService.createReply(requireSessionUserId(), boardId, reqDTO);
        return ReplyResponse.CreateAjaxDTO.from(reply, boardId);
    }

    @ResponseBody
    @PostMapping("/{replyId}/update")
    public ReplyResponse.UpdateAjaxDTO update(@PathVariable("boardId") Integer boardId,
            @PathVariable("replyId") Integer replyId,
            ReplyRequest.UpdateDTO reqDTO) {

        replyService.updateReply(requireSessionUserId(), replyId, reqDTO);
        return ReplyResponse.UpdateAjaxDTO.of(boardId, replyId, reqDTO.getContent());
    }

    @PostMapping("/{replyId}/delete")
    public String delete(@PathVariable("boardId") Integer boardId,
            @PathVariable("replyId") Integer replyId) {
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
