package com.example.travel_platform.trip;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequestMapping("/trip")
@Controller
public class TripController {

    private final TripService tripService;
    private final String kakaoMapAppKey;

    public TripController(TripService tripService, 
            @org.springframework.beans.factory.annotation.Value("${KAKAO_MAP_APP_KEY:}") String kakaoMapAppKey) {
        this.tripService = tripService;
        this.kakaoMapAppKey = kakaoMapAppKey;
    }

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
    public String tripDetailPage(@RequestParam(value = "id", required = false) Integer id, HttpSession session, Model model) {
        if (id != null) {
            User sessionUser = (User) session.getAttribute("sessionUser");
            if (sessionUser == null) {
                return "redirect:/login-form";
            }
            TripResponse.PlanDetailDTO plan = tripService.getPlanDetail(sessionUser.getId(), id);
            model.addAttribute("plan", plan);
        }
        return "pages/trip-detail";
    }

    @GetMapping("/place")
    public String tripAddPlacePage(@RequestParam(value = "id", required = false) Integer id, HttpSession session, Model model) {
        model.addAttribute("kakaoMapAppKey", kakaoMapAppKey == null ? "" : kakaoMapAppKey);
        
        if (id != null) {
            User sessionUser = (User) session.getAttribute("sessionUser");
            if (sessionUser == null) {
                return "redirect:/login-form";
            }
            TripResponse.PlanDetailDTO plan = tripService.getPlanDetail(sessionUser.getId(), id);
            model.addAttribute("plan", plan);
        }
        
        return "pages/trip-add-place";
    }
}
