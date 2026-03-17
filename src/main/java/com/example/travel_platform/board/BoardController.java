package com.example.travel_platform.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final HttpSession session;

    @GetMapping
    public String boardlist(@RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        BoardResponse.BoardListPageDTO responseDTO = boardService.getBoardList(category, page);
        model.addAttribute("model", responseDTO);
        return "pages/board-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("page", BoardResponse.BoardFormDTO.empty());
        return "pages/board-create";
    }

    @PostMapping
    public String create(@Valid BoardRequest.CreateBoardDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("page", BoardResponse.BoardFormDTO.fromCreate(
                    reqDTO,
                    getFieldError(bindingResult, "category"),
                    getFieldError(bindingResult, "title"),
                    getFieldError(bindingResult, "content")));
            return "pages/board-create";
        }

        boardService.createBoard(requireSessionUserId(), reqDTO);
        return "redirect:/boards";
    }

    @GetMapping("/{boardId}")
    public String detail(@PathVariable("boardId") Integer boardId, Model model) {
        Integer sessionUserId = resolveSessionUserIdOrNull();
        BoardResponse.BoardDetailDTO detailDTO = boardService.getBoardDetail(sessionUserId, boardId);
        model.addAttribute("board", detailDTO);
        return "pages/board-detail";
    }

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable("boardId") Integer boardId, Model model) {
        Integer sessionUserId = resolveSessionUserIdOrNull();
        BoardResponse.BoardDetailDTO board = boardService.getBoardDetail(sessionUserId, boardId);
        model.addAttribute("page", BoardResponse.BoardFormDTO.fromDetail(board));
        return "pages/board-edit";
    }

    @PostMapping("/{boardId}/update")
    public String update(@PathVariable("boardId") Integer boardId,
            @Valid BoardRequest.UpdateBoardDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            Integer sessionUserId = resolveSessionUserIdOrNull();
            BoardResponse.BoardDetailDTO board = boardService.getBoardDetail(sessionUserId, boardId);
            model.addAttribute("page", BoardResponse.BoardFormDTO.fromUpdate(
                    board,
                    reqDTO,
                    getFieldError(bindingResult, "category"),
                    getFieldError(bindingResult, "title"),
                    getFieldError(bindingResult, "content")));
            return "pages/board-edit";
        }

        boardService.updateBoard(requireSessionUserId(), boardId, reqDTO);
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{boardId}/delete")
    public String delete(@PathVariable("boardId") Integer boardId) {
        boardService.deleteBoard(requireSessionUserId(), boardId);
        return "redirect:/boards";
    }

    @PostMapping("/{boardId}/likes/toggle")
    @ResponseBody
    public BoardResponse.ToggleLikeDTO toggleLikeDTO(@PathVariable("boardId") Integer boardId) {
        Integer sessionUserId = requireSessionUserId();
        return boardService.toggleBoardLike(sessionUserId, boardId);
    }

    private Integer requireSessionUserId() {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        return sessionUser.getId();
    }

    private Integer resolveSessionUserIdOrNull() {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return null;
        }
        return sessionUser.getId();
    }

    private String getFieldError(BindingResult bindingResult, String field) {
        if (!bindingResult.hasFieldErrors(field)) {
            return null;
        }
        return bindingResult.getFieldError(field).getDefaultMessage();
    }
}
