package com.example.travel_platform.mypage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform.user.SessionUsers;
import com.example.travel_platform.user.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;
    private final UserService userService;
    private final HttpSession session;

    @GetMapping
    public String showMainPage(Model model) {
        Integer sessionUserId = requireSessionUserId();
        renderMainPage(model, sessionUserId, null, false, null, false);
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
            renderMainPage(model, sessionUserId, e.getMessage(), true, null, false);
            return "pages/mypage";
        }

        redirectAttributes.addFlashAttribute("passwordSuccessMessage", "비밀번호가 변경되었습니다.");
        return "redirect:/mypage";
    }

    @PostMapping("/withdraw")
    public String withdrawAccount(MypageRequest.WithdrawDTO reqDTO, Model model) {
        Integer sessionUserId = requireSessionUserId();

        try {
            userService.withdrawAccount(sessionUserId, reqDTO.getCurrentPassword());
        } catch (Exception400 | Exception403 e) {
            renderMainPage(model, sessionUserId, null, false, e.getMessage(), true);
            return "pages/mypage";
        }

        session.invalidate();
        return "redirect:/login-form";
    }

    @GetMapping("/bookings/{bookingId}")
    public String showBookingDetailPage(@PathVariable(name = "bookingId") Integer bookingId, Model model) {
        model.addAttribute("page", MypageResponse.BookingDetailPageDTO.of(bookingId));
        return "pages/booking-detail";
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }

    private void renderMainPage(
            Model model,
            Integer sessionUserId,
            String passwordError,
            boolean passwordModalOpen,
            String withdrawError,
            boolean withdrawModalOpen) {
        model.addAttribute("page", mypageService.getMainPage(sessionUserId));
        model.addAttribute("passwordError", passwordError);
        model.addAttribute("passwordModalOpen", passwordModalOpen);
        model.addAttribute("withdrawError", withdrawError);
        model.addAttribute("withdrawModalOpen", withdrawModalOpen);
    }
}
