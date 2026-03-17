package com.example.travel_platform.trip;

import java.util.List;

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

@RequiredArgsConstructor
@RequestMapping("/trip")
@Controller
public class TripController {

    private final TripService tripService;

    @GetMapping()
    public String tripListPage(@RequestParam(value = "category", defaultValue = "result") String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session,
            Model model) {

        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        Integer sessionUserId = sessionUser.getId();

        TripResponse.PlanListPageDTO pageDTO = tripService.getPlanList(sessionUserId, category, page);
        model.addAttribute("tripPlans", pageDTO.getPlans());
        model.addAttribute("pageDTO", pageDTO);

        model.addAttribute("isResult", "result".equals(category));
        model.addAttribute("isUpcoming", "upcoming".equals(category)); // 예정된 여행
        model.addAttribute("isPast", "past".equals(category)); // 다녀온 여행
        model.addAttribute("category", category);
        return "pages/trip-list";
    }

    @GetMapping("/create")
    public String tripCreatePage() {
        return "pages/trip-create";
    }

    @GetMapping("/detail")
    public String tripDetailPage() {
        return "pages/trip-detail";
    }

    @GetMapping("/place")
    public String tripAddPlacePage() {
        return "pages/trip-add-place";
    }

    // TripController.java (지윤)
    @PostMapping("/create")
    public String createPlan(TripRequest.CreatePlanDTO reqDTO, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null)
            return "redirect:/login-form";

        tripService.createPlan(sessionUser.getId(), reqDTO);
        return "redirect:/trip"; // 저장 후 목록 페이지로 이동
    }
}
