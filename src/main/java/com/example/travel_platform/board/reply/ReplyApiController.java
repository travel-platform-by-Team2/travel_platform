package com.example.travel_platform.board.reply;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards/{boardId}/replies")
@RequiredArgsConstructor
public class ReplyApiController {

    private final ReplyService replyService;
    private final HttpSession session;

    @PostMapping("")
    public ResponseEntity<Resp<ReplyResponse.CreatedDTO>> create(
            @PathVariable(name = "boardId") Integer boardId,
            @Valid @RequestBody ReplyRequest.CreateDTO reqDTO) {
        return Resp.ok(replyService.createReply(requireSessionUserId(), boardId, reqDTO));
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<Resp<ReplyResponse.UpdatedDTO>> update(
            @PathVariable(name = "boardId") Integer boardId,
            @PathVariable(name = "replyId") Integer replyId,
            @Valid @RequestBody ReplyRequest.UpdateDTO reqDTO) {
        return Resp.ok(replyService.updateReply(requireSessionUserId(), boardId, replyId, reqDTO));
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }
}
