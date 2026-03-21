package com.example.travel_platform.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String MODEL = "model";
    private static final String MODELS = "models";
    private static final String DASHBOARD_MENU = "dashboard";
    private static final String USERS_MENU = "users";
    private static final String BOARDS_MENU = "boards";
    private static final String DASHBOARD_VIEW = "pages/admin-dashboard";
    private static final String USERS_VIEW = "pages/admin-users";
    private static final String BOARDS_VIEW = "pages/admin-boards";
    private static final String BOARDS_REDIRECT = "redirect:/admin/boards";

    private final AdminService adminService;
    private final HttpSession session;

    @GetMapping("")
    public String dashboard(Model model) {
        return renderDashboardPage(model, adminService.getDashboardView());
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            Model model) {
        AdminResponse.UserListViewDTO viewDTO = adminService.getUserListView(active, keyword, sortBy, orderBy);
        applyUserTabHrefs(viewDTO.getModel());
        return renderUsersPage(model, viewDTO);
    }

    @PostMapping("/users/{userId}/status")
    public String toggleUserStatus(
            @PathVariable("userId") Integer userId,
            @RequestParam(name = "targetActive") boolean targetActive,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            RedirectAttributes redirectAttributes) {
        adminService.updateUserActiveStatus(userId, targetActive);

        if (active != null) {
            redirectAttributes.addAttribute("active", active);
        }
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }
        if (sortBy != null && !sortBy.isBlank()) {
            redirectAttributes.addAttribute("sortBy", sortBy);
        }
        if (orderBy != null && !orderBy.isBlank()) {
            redirectAttributes.addAttribute("orderBy", orderBy);
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/boards")
    public String boards(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        return renderBoardsPage(model, adminService.getBoardListView(category, keyword, sort, page));
    }

    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable(name = "boardId") Integer boardId) {
        adminService.deleteBoardByAdmin(requiredSessionUser(), boardId);
        return BOARDS_REDIRECT;
    }

    private String renderDashboardPage(Model model, AdminResponse.DashboardViewDTO page) {
        model.addAttribute(MODEL, page.applyCurrentMenu(DASHBOARD_MENU));
        return DASHBOARD_VIEW;
    }

    private String renderUsersPage(Model model, AdminResponse.UserListViewDTO viewDTO) {
        model.addAttribute(MODEL, viewDTO.getModel().applyCurrentMenu(USERS_MENU));
        model.addAttribute(MODELS, viewDTO.getModels());
        return USERS_VIEW;
    }

    private String renderBoardsPage(Model model, AdminResponse.BoardListViewDTO viewDTO) {
        model.addAttribute(MODEL, viewDTO.getModel().applyCurrentMenu(BOARDS_MENU));
        model.addAttribute(MODELS, viewDTO.getModels());
        return BOARDS_VIEW;
    }

    private void applyUserTabHrefs(AdminResponse.UserListPageDTO pageModel) {
        pageModel.applyTabHrefs(
                buildUsersUrl(null, pageModel.getKeyword(), pageModel.getSortBy(), pageModel.getOrderBy()),
                buildUsersUrl(true, pageModel.getKeyword(), pageModel.getSortBy(), pageModel.getOrderBy()),
                buildUsersUrl(false, pageModel.getKeyword(), pageModel.getSortBy(), pageModel.getOrderBy()));
    }

    private SessionUser requiredSessionUser() {
        return SessionUsers.require(session);
    }

    private String buildUsersUrl(Boolean active, String keyword, String sortBy, String orderBy) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/users");
        if (active != null) {
            builder.queryParam("active", active);
        }
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword.trim());
        }
        if (sortBy != null && !sortBy.isBlank()) {
            builder.queryParam("sortBy", sortBy);
        }
        if (orderBy != null && !orderBy.isBlank()) {
            builder.queryParam("orderBy", orderBy);
        }
        return builder.toUriString();
    }
}
