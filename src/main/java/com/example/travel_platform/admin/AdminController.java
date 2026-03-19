package com.example.travel_platform.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform.user.User;

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
        return "pages/admin-dashboard";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {
        applySidebarState(model, "users");
        model.addAttribute("users", adminService.getAdminUsers(active, keyword));
        model.addAttribute("totalUserCount", adminService.getTotalUserCount());
        model.addAttribute("inactiveUserCount", adminService.getInactiveUserCount());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("currentActive", active);
        model.addAttribute("isAllTab", active == null);
        model.addAttribute("isActiveTab", Boolean.TRUE.equals(active));
        model.addAttribute("isInactiveTab", Boolean.FALSE.equals(active));
        return "pages/admin-users";
    }

    @GetMapping("/boards")
    public String boards(@RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        AdminResponse.AdminBoardListDTO responseDTO = adminService.getBoardList(category, keyword, page);
        model.addAttribute("model", responseDTO);
        applySidebarState(model, "boards");
        return "pages/admin-boards";
    }

    @PostMapping("/boards/{boardId}/delete")
    public String deleteBoard(@PathVariable("boardId") Integer boardId) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        adminService.deleteBoard(sessionUser, boardId);
        return "redirect:/admin/boards";
    }

    private void applySidebarState(Model model, String currentMenu) {
        model.addAttribute("dashboardActiveClass", isCurrentMenu(currentMenu, "dashboard"));
        model.addAttribute("usersActiveClass", isCurrentMenu(currentMenu, "users"));
        model.addAttribute("lodgingsActiveClass", isCurrentMenu(currentMenu, "lodgings"));
        model.addAttribute("boardsActiveClass", isCurrentMenu(currentMenu, "boards"));
    }

    private String isCurrentMenu(String currentMenu, String targetMenu) {
        return targetMenu.equals(currentMenu) ? " is-active" : "";
    }
}
