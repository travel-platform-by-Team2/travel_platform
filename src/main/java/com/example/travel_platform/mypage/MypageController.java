package com.example.travel_platform.mypage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;
    private final HttpSession session;

    @GetMapping
    public String mypage(Model model) {
        Integer sessionUserId = requireSessionUserId();
        model.addAttribute("page", mypageService.getMainPage(sessionUserId));
        return "pages/mypage";
    }

    @PostMapping("/password")
    public String changePassword(
            MypageRequest.ChangePasswordDTO reqDTO,
            Model model,
            RedirectAttributes redirectAttributes) {
        Integer sessionUserId = requireSessionUserId();

        try {
            mypageService.changePassword(sessionUserId, reqDTO);
        } catch (Exception400 e) {
            model.addAttribute("page", mypageService.getMainPage(sessionUserId));
            model.addAttribute("passwordError", e.getMessage());
            model.addAttribute("passwordModalOpen", true);
            return "pages/mypage";
        }

        redirectAttributes.addFlashAttribute("passwordSuccessMessage", "비밀번호가 변경되었습니다.");
        return "redirect:/mypage";
    }

    @GetMapping("/booking")
    public String bookingDetail() {
        return "pages/booking-detail";
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }
}
