package com.example.travel_platform.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform.user.SessionUsers;

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
    public String list(@RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        BoardResponse.ListPageDTO responseDTO = boardService.getBoardList(category, keyword, sort, page);
        model.addAttribute("model", responseDTO);
        return "pages/board-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("page", BoardResponse.FormDTO.empty());
        return "pages/board-create";
    }

    @PostMapping
    public String create(@Valid BoardRequest.CreateDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("page", BoardResponse.FormDTO.fromCreate(
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
    public String detail(@PathVariable(name = "boardId") Integer boardId, Model model) {
        Integer sessionUserId = resolveSessionUserIdOrNull();
        BoardResponse.DetailDTO detailDTO = boardService.getBoardDetail(sessionUserId, boardId);
        model.addAttribute("board", detailDTO);
        return "pages/board-detail";
    }

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable(name = "boardId") Integer boardId, Model model) {
        Integer sessionUserId = requireSessionUserId();
        BoardResponse.FormDTO formDTO = boardService.getBoardForm(sessionUserId, boardId);
        model.addAttribute("page", formDTO);
        return "pages/board-edit";
    }

    @PostMapping("/{boardId}/update")
    public String update(@PathVariable(name = "boardId") Integer boardId,
            @Valid BoardRequest.UpdateDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            Integer sessionUserId = requireSessionUserId();
            BoardResponse.FormDTO formDTO = boardService.getBoardForm(sessionUserId, boardId);
            model.addAttribute("page", BoardResponse.FormDTO.fromUpdate(
                    formDTO.getId(),
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
    public String delete(@PathVariable(name = "boardId") Integer boardId) {
        boardService.deleteBoard(requireSessionUserId(), boardId);
        return "redirect:/boards";
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }

    private Integer resolveSessionUserIdOrNull() {
        return SessionUsers.getUserIdOrNull(session);
    }

    private String getFieldError(BindingResult bindingResult, String field) {
        if (!bindingResult.hasFieldErrors(field)) {
            return null;
        }
        return bindingResult.getFieldError(field).getDefaultMessage();
    }
}
