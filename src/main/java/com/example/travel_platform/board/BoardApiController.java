package com.example.travel_platform.board;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardApiController {

    private final BoardService boardService;
    private final HttpSession session;

    @PostMapping("/{boardId}/likes/toggle")
    public ResponseEntity<Resp<BoardResponse.LikeToggleDTO>> toggleLike(
            @PathVariable(name = "boardId") Integer boardId) {
        Integer sessionUserId = requireSessionUserId();
        return Resp.ok(boardService.toggleBoardLike(sessionUserId, boardId));
    }

    private Integer requireSessionUserId() {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        return sessionUser.getId();
    }
}
