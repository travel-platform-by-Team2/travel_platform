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

    private static final String MODEL_PAGE = "page";
    private static final String MODEL_PLAN = "plan";

    private static final String VIEW_LIST = "pages/trip-list";
    private static final String VIEW_CREATE = "pages/trip-create";
    private static final String VIEW_DETAIL = "pages/trip-detail";
    private static final String VIEW_PLACE = "pages/trip-add-place";

    private static final String REDIRECT_PREFIX = "redirect:";

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
        TripResponse.ListPageDTO pageDTO = tripService.getPlanList(requiredSessionUserId(), category, page);
        return renderPage(model, pageDTO, VIEW_LIST);
    }

    @GetMapping("/create")
    public String tripCreatePage(Model model) {
        return renderCreateForm(model, TripResponse.CreateFormDTO.empty());
    }

    @GetMapping("/detail")
    public String tripDetailPage(@RequestParam(name = "id") Integer id,
            Model model) {
        TripResponse.DetailDTO detailDTO = tripService.getPlanDetail(requiredSessionUserId(), id);
        return renderDetail(model, detailDTO);
    }

    @GetMapping("/place")
    public String tripAddPlacePage(@RequestParam(name = "id") Integer id,
            Model model) {
        TripResponse.PlacePageDTO pageDTO = tripService.getPlacePage(requiredSessionUserId(), id, kakaoMapAppKey);
        return renderPage(model, pageDTO, VIEW_PLACE);
    }

    @PostMapping("/create")
    public String createPlan(@Valid TripRequest.CreatePlanDTO reqDTO,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return renderCreateForm(model, createFormPage(reqDTO, bindingResult));
        }

        TripResponse.CreatedDTO createdDTO = tripService.createPlan(requiredSessionUserId(), reqDTO);
        return redirect(createdDTO.getRedirectUrl());
    }

    private String renderPage(Model model, Object pageDTO, String view) {
        model.addAttribute(MODEL_PAGE, pageDTO);
        return view;
    }

    private String renderCreateForm(Model model, TripResponse.CreateFormDTO formDTO) {
        return renderPage(model, formDTO, VIEW_CREATE);
    }

    private String renderDetail(Model model, TripResponse.DetailDTO detailDTO) {
        model.addAttribute(MODEL_PLAN, detailDTO);
        return VIEW_DETAIL;
    }

    private TripResponse.CreateFormDTO createFormPage(TripRequest.CreatePlanDTO reqDTO, BindingResult bindingResult) {
        return TripResponse.CreateFormDTO.from(
                reqDTO.getTitle(),
                reqDTO.getRegion(),
                reqDTO.getWhoWith(),
                reqDTO.getStartDate(),
                reqDTO.getEndDate(),
                getFieldError(bindingResult, "title"),
                getFieldError(bindingResult, "region"),
                getFieldError(bindingResult, "whoWith"),
                getFieldError(bindingResult, "startDate"),
                getFieldError(bindingResult, "endDate"));
    }

    private String redirect(String path) {
        return REDIRECT_PREFIX + path;
    }

    private Integer requiredSessionUserId() {
        return SessionUsers.requireUserId(session);
    }

    private String getFieldError(BindingResult bindingResult, String field) {
        if (!bindingResult.hasFieldErrors(field)) {
            return null;
        }
        return bindingResult.getFieldError(field).getDefaultMessage();
    }
}
