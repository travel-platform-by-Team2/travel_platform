package com.example.travel_platform.board;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String boardlist(@RequestParam(value = "category", required = false) String category, Model model) {
        List<BoardResponse.BoardSummaryDTO> boards = boardService.getBoardList(category);
        model.addAttribute("boards", boards);
        model.addAttribute("selectedCategory", category);

        model.addAttribute("isTips", "tips".equals(category));
        model.addAttribute("isPlan", "plan".equals(category));
        model.addAttribute("isFood", "food".equals(category));
        model.addAttribute("isReview", "review".equals(category));
        model.addAttribute("isQna", "qna".equals(category));
        return "pages/board-list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "");
        model.addAttribute("content", "");
        model.addAttribute("category", "");
        return "pages/board-create";
    }

    @PostMapping
    public String create(@Valid BoardRequest.CreateBoardDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", reqDTO.getTitle()); // 빈칸 입력시 오류발생
            model.addAttribute("content", reqDTO.getContent());
            model.addAttribute("category", reqDTO.getCategory());

            if (bindingResult.hasFieldErrors("category")) {
                model.addAttribute("categoryerror",
                        bindingResult.getFieldError("category").getDefaultMessage());
            }

            if (bindingResult.hasFieldErrors("title")) {
                model.addAttribute("titleError",
                        bindingResult.getFieldError("title").getDefaultMessage());
            }

            if (bindingResult.hasFieldErrors("content")) {
                model.addAttribute("contentError",
                        bindingResult.getFieldError("content").getDefaultMessage());
            }

            return "pages/board-create";
        }

        boardService.createBoard(requireSessionUserId(), reqDTO);
        return "redirect:/boards";
    }

    @GetMapping("/{boardId}")
    public String detail(@PathVariable("boardId") Integer boardId, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        Integer sessionUserId = null;
        if (sessionUser != null) {
            sessionUserId = sessionUser.getId();
        }

        BoardResponse.BoardDetailDTO detailDTO = boardService.getBoardDetail(sessionUserId, boardId);
        model.addAttribute("board", detailDTO);
        return "pages/board-detail";
    }

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable("boardId") Integer boardId, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        Integer sessionUserId = null;
        if (sessionUser != null) {
            sessionUserId = sessionUser.getId();
        }
        model.addAttribute("board", boardService.getBoardDetail(sessionUserId, boardId));
        return "pages/board-edit";
    }

    @PostMapping("/{boardId}/update")
    public String update(@PathVariable("boardId") Integer boardId,
            @Valid BoardRequest.UpdateBoardDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            User sessionUser = (User) session.getAttribute("sessionUser");

            Integer sessionUserId = null;
            if (sessionUser != null) {
                sessionUserId = sessionUser.getId();
            }

            BoardResponse.BoardDetailDTO board = boardService.getBoardDetail(sessionUserId, boardId);

            // 사용자가 방금 입력한 값으로 다시 덮어쓰기
            board.setTitle(reqDTO.getTitle());
            board.setContent(reqDTO.getContent());
            board.setCategory(reqDTO.getCategory());

            if (bindingResult.hasFieldErrors("category")) {
                board.setCategoryError(bindingResult.getFieldError("category").getDefaultMessage());
            }

            if (bindingResult.hasFieldErrors("title")) {
                board.setTitleError(bindingResult.getFieldError("title").getDefaultMessage());
            }

            if (bindingResult.hasFieldErrors("content")) {
                board.setContentError(bindingResult.getFieldError("content").getDefaultMessage());
            }

            model.addAttribute("board", board);
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

    private Integer requireSessionUserId() {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요합니다.");
        }
        return sessionUser.getId();
    }

}
