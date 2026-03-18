package com.example.travel_platform.trip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RequestMapping("/trip")
@Controller
public class TripController {

    private final TripService tripService;
    private final HttpSession session;
    private final String kakaoMapAppKey;

    public TripController(TripService tripService,
            HttpSession session,
            @Value("${KAKAO_MAP_APP_KEY:}") String kakaoMapAppKey) {
        this.tripService = tripService;
        this.session = session;
        this.kakaoMapAppKey = kakaoMapAppKey;
    }

    @GetMapping()
    public String tripListPage(@RequestParam(name = "category", defaultValue = "result") String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        TripResponse.ListPageDTO pageDTO = tripService.getPlanList(requireSessionUserId(), category, page);
        model.addAttribute("page", pageDTO);
        return "pages/trip-list";
    }

    @GetMapping("/create")
    public String tripCreatePage(Model model) {
        model.addAttribute("page", TripResponse.CreateFormDTO.empty());
        return "pages/trip-create";
    }

    @GetMapping("/detail")
    public String tripDetailPage(@RequestParam(name = "id") Integer id,
            Model model) {
        TripResponse.DetailDTO detailDTO = tripService.getPlanDetail(requireSessionUserId(), id);
        model.addAttribute("page", TripResponse.DetailPageDTO.of(detailDTO));
        return "pages/trip-detail";
    }

    @GetMapping("/place")
    public String tripAddPlacePage(@RequestParam(name = "id") Integer id,
            Model model) {
        TripResponse.DetailDTO detailDTO = tripService.getPlanDetail(requireSessionUserId(), id);
        model.addAttribute("page", TripResponse.PlacePageDTO.of(detailDTO, kakaoMapAppKey));
        return "pages/trip-add-place";
    }

    @PostMapping("/create")
    public String createPlan(@Valid TripRequest.CreatePlanDTO reqDTO,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("page", TripResponse.CreateFormDTO.from(
                    reqDTO,
                    getFieldError(bindingResult, "title"),
                    getFieldError(bindingResult, "region"),
                    getFieldError(bindingResult, "whoWith"),
                    getFieldError(bindingResult, "startDate"),
                    getFieldError(bindingResult, "endDate")));
            return "pages/trip-create";
        }

        TripResponse.CreatedDTO createdDTO = tripService.createPlan(requireSessionUserId(), reqDTO);
        return "redirect:" + createdDTO.getRedirectUrl();
    }

    private Integer requireSessionUserId() {
        return SessionUsers.requireUserId(session);
    }

    private String getFieldError(BindingResult bindingResult, String field) {
        if (!bindingResult.hasFieldErrors(field)) {
            return null;
        }
        return bindingResult.getFieldError(field).getDefaultMessage();
    }
}
