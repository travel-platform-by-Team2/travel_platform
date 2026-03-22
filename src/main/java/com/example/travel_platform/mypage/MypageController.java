package com.example.travel_platform.mypage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final String MODEL = "model";
    private static final String MODELS = "models";
    private static final String MAIN_PAGE_VIEW = "pages/mypage";
    private static final String BOOKING_LIST_VIEW = "pages/booking-list";
    private static final String BOOKING_DETAIL_VIEW = "pages/booking-detail";
    private static final String REDIRECT_MAIN_PAGE = "redirect:/mypage";
    private static final String REDIRECT_LOGIN_FORM = "redirect:/login-form";

    private final MypageService mypageService;
    private final UserService userService;
    private final HttpSession session;

    @GetMapping
    public String showMainPage(
            @ModelAttribute(name = "passwordSuccessMessage") String passwordSuccessMessage,
            Model model) {
        return renderMainPage(model, requireSessionUserId(), passwordSuccessMessage);
    }

    @GetMapping("/bookings")
    public String showBookingListPage(
            @RequestParam(name = "category", required = false) String category,
            Model model) {
        return renderBookingListPage(model, requireSessionUserId(), category);
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
            return renderPasswordFailure(model, sessionUserId, e.getMessage());
        }

        redirectAttributes.addFlashAttribute("passwordSuccessMessage", "비밀번호가 변경되었습니다.");
        return REDIRECT_MAIN_PAGE;
    }

    @PostMapping("/withdraw")
    public String withdrawAccount(MypageRequest.WithdrawDTO reqDTO, Model model) {
        Integer sessionUserId = requireSessionUserId();

        try {
            userService.withdrawAccount(sessionUserId, reqDTO.getCurrentPassword());
        } catch (Exception400 | Exception403 e) {
            return renderWithdrawFailure(model, sessionUserId, e.getMessage());
        }

        session.invalidate();
        return REDIRECT_LOGIN_FORM;
    }

    @GetMapping("/bookings/{bookingId}")
    public String showBookingDetailPage(@PathVariable(name = "bookingId") Integer bookingId, Model model) {
        return renderBookingDetailPage(model, requireSessionUserId(), bookingId);
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }

    private String renderMainPage(Model model, Integer sessionUserId) {
        return renderMainPage(model, mypageService.getMainPage(sessionUserId));
    }

    private String renderMainPage(Model model, Integer sessionUserId, String passwordSuccessMessage) {
        return renderMainPage(model, mypageService.getMainPage(sessionUserId).withPasswordSuccess(passwordSuccessMessage));
    }

    private String renderPasswordFailure(Model model, Integer sessionUserId, String errorMessage) {
        return renderMainPage(model, mypageService.getMainPage(sessionUserId).openPasswordModal(errorMessage));
    }

    private String renderWithdrawFailure(Model model, Integer sessionUserId, String errorMessage) {
        return renderMainPage(model, mypageService.getMainPage(sessionUserId).openWithdrawModal(errorMessage));
    }

    private String renderMainPage(Model model, MypageResponse.MainPageDTO page) {
        model.addAttribute(MODEL, page);
        return MAIN_PAGE_VIEW;
    }

    private String renderBookingListPage(Model model, Integer sessionUserId, String category) {
        MypageResponse.BookingListViewDTO view = mypageService.getBookingListView(sessionUserId, category);
        model.addAttribute(MODEL, view.getPage());
        model.addAttribute(MODELS, view.getItems());
        return BOOKING_LIST_VIEW;
    }

    private String renderBookingDetailPage(Model model, Integer sessionUserId, Integer bookingId) {
        model.addAttribute(MODEL, mypageService.getBookingDetailPage(sessionUserId, bookingId));
        return BOOKING_DETAIL_VIEW;
    }
}
