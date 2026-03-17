package com.example.travel_platform.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("")
    public String dashboard(Model model) {
        applySidebarState(model, "dashboard");
        return "pages/admin-dashboard";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,
            Model model) {
        applySidebarState(model, "users");
        model.addAttribute("users", adminService.getAdminUsers(active, keyword));
        model.addAttribute("totalUserCount", adminService.getTotalUserCount());
        model.addAttribute("inactiveUserCount", adminService.getInactiveUserCount());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentActive", active);
        model.addAttribute("isAllTab", active == null);
        model.addAttribute("isActiveTab", Boolean.TRUE.equals(active));
        model.addAttribute("isInactiveTab", Boolean.FALSE.equals(active));
        return "pages/admin-users";
    }

    @GetMapping("/lodgings")
    public String lodgings(Model model) {
        applySidebarState(model, "lodgings");
        return "pages/admin-lodgings";
    }

    @GetMapping("/boards")
    public String boards(Model model) {
        applySidebarState(model, "boards");
        return "pages/admin-boards";
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
