package com.example.travel_platform.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("")
    public String dashboard(Model model) {
        applySidebarState(model, "dashboard");
        return "pages/admin-dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        applySidebarState(model, "users");
        return "pages/admin-users";
    }

    @GetMapping("/lodgings")
    public String lodgings(Model model) {
        applySidebarState(model, "lodgings");
        return "pages/admin-lodgings";
    }

    @GetMapping("/boards")
    public String boards(@RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        AdminResponse.AdminBoardListDTO responseDTO = adminService.getBoardList(category, keyword, page);

        model.addAttribute("model", responseDTO);
        model.addAttribute("totalCount", responseDTO.getAllCount());
        model.addAttribute("selectCategory", category);

        model.addAttribute("isTips", "tips".equals(category));
        model.addAttribute("isPlan", "plan".equals(category));
        model.addAttribute("isFood", "food".equals(category));
        model.addAttribute("isReview", "review".equals(category));
        model.addAttribute("isQna", "qna".equals(category));

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
