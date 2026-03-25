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

    private static final String MODEL = "model";
    private static final String MODELS = "models";

    private static final String VIEW_LIST = "pages/board-list";
    private static final String VIEW_CREATE = "pages/board-create";
    private static final String VIEW_DETAIL = "pages/board-detail";
    private static final String VIEW_EDIT = "pages/board-edit";

    private static final String REDIRECT_LIST = "redirect:/boards";

    private final BoardService boardService;
    private final HttpSession session;

    @GetMapping
    public String list(@RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        BoardResponse.ListViewDTO responseDTO = boardService.getBoardList(category, keyword, sort, page);
        return renderList(model, responseDTO);
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        return renderCreateForm(model, BoardResponse.FormDTO.empty());
    }

    @PostMapping
    public String create(@Valid BoardRequest.CreateDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return renderCreateForm(model, createFormPage(reqDTO, bindingResult));
        }

        boardService.createBoard(requiredSessionUserId(), reqDTO);
        return REDIRECT_LIST;
    }

    @GetMapping("/{boardId}")
    public String detail(@PathVariable(name = "boardId") Integer boardId, Model model) {
        Integer sessionUserId = optionalSessionUserId();
        BoardResponse.DetailDTO detailDTO = boardService.getBoardDetail(sessionUserId, boardId);
        return renderDetail(model, detailDTO);
    }

    @GetMapping("/{boardId}/edit")
    public String editForm(@PathVariable(name = "boardId") Integer boardId, Model model) {
        Integer sessionUserId = requiredSessionUserId();
        BoardResponse.FormDTO formDTO = boardService.getBoardForm(sessionUserId, boardId);
        return renderEditForm(model, formDTO);
    }

    @PostMapping("/{boardId}/update")
    public String update(@PathVariable(name = "boardId") Integer boardId,
            @Valid BoardRequest.UpdateDTO reqDTO,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return renderEditForm(model, updateFormPage(boardId, reqDTO, bindingResult));
        }

        boardService.updateBoard(requiredSessionUserId(), boardId, reqDTO);
        return "redirect:/boards/" + boardId;
    }

    @PostMapping("/{boardId}/delete")
    public String delete(@PathVariable(name = "boardId") Integer boardId) {
        boardService.deleteBoard(requiredSessionUserId(), boardId);
        return REDIRECT_LIST;
    }

    private String renderList(Model model, BoardResponse.ListViewDTO responseDTO) {
        model.addAttribute(MODEL, responseDTO.getModel());
        model.addAttribute(MODELS, responseDTO.getModels());
        return VIEW_LIST;
    }

    private String renderCreateForm(Model model, BoardResponse.FormDTO formDTO) {
        model.addAttribute(MODEL, formDTO);
        return VIEW_CREATE;
    }

    private String renderDetail(Model model, BoardResponse.DetailDTO detailDTO) {
        model.addAttribute(MODEL, detailDTO);
        return VIEW_DETAIL;
    }

    private String renderEditForm(Model model, BoardResponse.FormDTO formDTO) {
        model.addAttribute(MODEL, formDTO);
        return VIEW_EDIT;
    }

    private BoardResponse.FormDTO createFormPage(BoardRequest.CreateDTO reqDTO, BindingResult bindingResult) {
        return BoardResponse.FormDTO.fromCreateRequest(
                reqDTO,
                getFieldError(bindingResult, "category"),
                getFieldError(bindingResult, "title"),
                getFieldError(bindingResult, "content"));
    }

    private BoardResponse.FormDTO updateFormPage(Integer boardId,
            BoardRequest.UpdateDTO reqDTO,
            BindingResult bindingResult) {
        BoardResponse.FormDTO formDTO = boardService.getBoardForm(requiredSessionUserId(), boardId);
        return BoardResponse.FormDTO.fromUpdateRequest(
                formDTO.getId(),
                reqDTO,
                getFieldError(bindingResult, "category"),
                getFieldError(bindingResult, "title"),
                getFieldError(bindingResult, "content"));
    }

    private Integer requiredSessionUserId() {
        return SessionUsers.requireUserId(session);
    }

    private Integer optionalSessionUserId() {
        return SessionUsers.getUserIdOrNull(session);
    }

    private String getFieldError(BindingResult bindingResult, String field) {
        if (!bindingResult.hasFieldErrors(field)) {
            return null;
        }
        return bindingResult.getFieldError(field).getDefaultMessage();
    }
}
