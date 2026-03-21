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

    private static final String MODEL = "model";
    private static final String MODELS = "models";

    private static final String VIEW_LIST = "pages/trip-list";
    private static final String VIEW_CREATE = "pages/trip-create";
    private static final String VIEW_DETAIL = "pages/trip-detail";
    private static final String VIEW_PLACE = "pages/trip-add-place";

    private static final String REDIRECT_PREFIX = "redirect:";

    private final TripService tripService;
    private final HttpSession session;
    private final String kakaoMapAppKey;

    public TripController(
            TripService tripService,
            HttpSession session,
            @Value("${KAKAO_MAP_APP_KEY:}") String kakaoMapAppKey) {
        this.tripService = tripService;
        this.session = session;
        this.kakaoMapAppKey = kakaoMapAppKey;
    }

    @GetMapping()
    public String tripListPage(
            @RequestParam(name = "category", defaultValue = "result") String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        TripResponse.ListPageDTO modelDTO = tripService.getPlanList(requiredSessionUserId(), category, page);
        return renderListModel(model, modelDTO);
    }

    @GetMapping("/create")
    public String tripCreatePage(Model model) {
        return renderModel(model, TripResponse.CreateFormDTO.createEmptyForm(), VIEW_CREATE);
    }

    @GetMapping("/detail")
    public String tripDetailPage(
            @RequestParam(name = "id") Integer id,
            Model model) {
        TripResponse.DetailDTO modelDTO = tripService.getPlanDetail(requiredSessionUserId(), id);
        return renderModel(model, modelDTO, VIEW_DETAIL);
    }

    @GetMapping("/place")
    public String tripAddPlacePage(
            @RequestParam(name = "id") Integer id,
            Model model) {
        TripResponse.PlacePageDTO modelDTO = tripService.getPlacePage(requiredSessionUserId(), id, kakaoMapAppKey);
        return renderModel(model, modelDTO, VIEW_PLACE);
    }

    @PostMapping("/create")
    public String createPlan(
            @Valid TripRequest.CreatePlanDTO reqDTO,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return renderModel(model, createCreateForm(reqDTO, bindingResult), VIEW_CREATE);
        }

        TripResponse.CreatedDTO createdDTO = tripService.createPlan(requiredSessionUserId(), reqDTO);
        return redirect(createdDTO.getRedirectUrl());
    }

    private String renderModel(Model model, Object modelDTO, String view) {
        model.addAttribute(MODEL, modelDTO);
        return view;
    }

    private String renderListModel(Model model, TripResponse.ListPageDTO modelDTO) {
        model.addAttribute(MODEL, modelDTO);
        model.addAttribute(MODELS, modelDTO.getPlans());
        return VIEW_LIST;
    }

    private TripResponse.CreateFormDTO createCreateForm(
            TripRequest.CreatePlanDTO reqDTO,
            BindingResult bindingResult) {
        return TripResponse.CreateFormDTO.createCreateForm(
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
