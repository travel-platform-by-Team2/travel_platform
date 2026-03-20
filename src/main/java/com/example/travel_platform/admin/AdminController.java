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
    private final AdminService adminService;
    private final HttpSession session;

    @GetMapping("")
    public String dashboard(Model model) {
        applySidebarState(model, "dashboard");
        model.addAttribute("page", adminService.getDashboardPage());
        return "pages/admin-dashboard";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            Model model) {
        AdminResponse.UserListPageDTO page = adminService.getUsersPage(active, keyword, sortBy, orderBy);

        applySidebarState(model, "users");
        applyUsersPageModel(model, page);
        model.addAttribute("hasUsers", !page.getUsers().isEmpty());
        model.addAttribute("allTabHref", buildUsersUrl(null, page.getKeyword(), page.getSortBy(), page.getOrderBy()));
        model.addAttribute("activeTabHref", buildUsersUrl(true, page.getKeyword(), page.getSortBy(), page.getOrderBy()));
        model.addAttribute("inactiveTabHref", buildUsersUrl(false, page.getKeyword(), page.getSortBy(), page.getOrderBy()));
        return "pages/admin-users";
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
        adminService.updateUserActive(userId, targetActive);

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
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        AdminResponse.AdminBoardListDTO responseDTO = adminService.getBoardsPage(category, keyword, sort, page);
        model.addAttribute("model", responseDTO);
        applySidebarState(model, "boards");
        return "pages/admin-boards";
    }

    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable("boardId") Integer boardId) {
        SessionUser sessionUser = SessionUsers.getOrNull(session);
        adminService.deleteBoard(sessionUser, boardId);
        return "redirect:/admin/boards";
    }

    private void applySidebarState(Model model, String currentMenu) {
        model.addAttribute("dashboardActiveClass", isCurrentMenu(currentMenu, "dashboard"));
        model.addAttribute("usersActiveClass", isCurrentMenu(currentMenu, "users"));
        model.addAttribute("lodgingsActiveClass", isCurrentMenu(currentMenu, "lodgings"));
        model.addAttribute("boardsActiveClass", isCurrentMenu(currentMenu, "boards"));
    }

    private void applyUsersPageModel(Model model, AdminResponse.UserListPageDTO page) {
        model.addAttribute("users", page.getUsers());
        model.addAttribute("totalUserCount", page.getTotalUserCount());
        model.addAttribute("inactiveUserCount", page.getInactiveUserCount());
        model.addAttribute("keyword", page.getKeyword());
        model.addAttribute("currentActive", page.getCurrentActive());
        model.addAttribute("isAllTab", page.isAllTab());
        model.addAttribute("isActiveTab", page.isActiveTab());
        model.addAttribute("isInactiveTab", page.isInactiveTab());
        model.addAttribute("sortBy", page.getSortBy());
        model.addAttribute("orderBy", page.getOrderBy());
        model.addAttribute("isSortByPostCount", page.isSortByPostCount());
        model.addAttribute("isSortByCreatedAt", page.isSortByCreatedAt());
        model.addAttribute("isOrderByAsc", page.isOrderByAsc());
        model.addAttribute("isOrderByDesc", page.isOrderByDesc());
    }

    private String isCurrentMenu(String currentMenu, String targetMenu) {
        return targetMenu.equals(currentMenu) ? " is-active" : "";
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
