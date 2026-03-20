package com.example.travel_platform.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

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
        return renderDashboardPage(model, adminService.getDashboardPage());
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {
        return renderUsersPage(model, adminService.getUsersPage(active, keyword));
    }

    @GetMapping("/boards")
    public String boards(@RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        return renderBoardsPage(model, adminService.getBoardsPage(category, keyword, sort, page));
    }

    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable(name = "boardId") Integer boardId) {
        SessionUser sessionUser = SessionUsers.require(session);
        adminService.deleteBoard(sessionUser, boardId);
        return BOARDS_REDIRECT;
    }

    private String renderDashboardPage(Model model, AdminResponse.DashboardPageDTO page) {
        model.addAttribute("page", page.withCurrentMenu(DASHBOARD_MENU));
        return DASHBOARD_VIEW;
    }

    private String renderUsersPage(Model model, AdminResponse.UserListPageDTO page) {
        model.addAttribute("page", page.withCurrentMenu(USERS_MENU));
        return USERS_VIEW;
    }

    private String renderBoardsPage(Model model, AdminResponse.AdminBoardListDTO page) {
        model.addAttribute("page", page.withCurrentMenu(BOARDS_MENU));
        return BOARDS_VIEW;
    }
}
