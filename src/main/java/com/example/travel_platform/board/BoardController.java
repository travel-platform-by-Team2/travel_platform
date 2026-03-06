package com.example.travel_platform.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final HttpSession session;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", boardService.getBoardList());
        return "pages/board-list";
    }

    @GetMapping("/new")
    public String createForm() {
        return "pages/board-create";
    }

    @PostMapping
    public String create(BoardRequest.CreateBoardDTO reqDTO) {
        boardService.createBoard(requireSessionUserId(), reqDTO);
        return "redirect:/boards";
    }

    @GetMapping("/{boardId}")
    public String detail(@PathVariable Integer boardId, Model model) {
        model.addAttribute("board", boardService.getBoardDetail(boardId));
        return "pages/board-detail";
    }

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable Integer boardId, Model model) {
        model.addAttribute("board", boardService.getBoardDetail(boardId));
        return "pages/board-edit";
    }

    @PostMapping("/{boardId}/update")
    public String update(@PathVariable Integer boardId, BoardRequest.UpdateBoardDTO reqDTO) {
        boardService.updateBoard(requireSessionUserId(), boardId, reqDTO);
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{boardId}/delete")
    public String delete(@PathVariable Integer boardId) {
        boardService.deleteBoard(requireSessionUserId(), boardId);
        return "redirect:/boards";
    }

    private Integer requireSessionUserId() {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        return sessionUser.getId();
    }
}
