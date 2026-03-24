package com.example.travel_platform.trip;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripApiController {

    private final TripService tripService;
    private final HttpSession session;

    @PostMapping
    public ResponseEntity<Resp<TripResponse.CreatedDTO>> createPlan(
            @Valid @RequestBody TripRequest.CreatePlanDTO reqDTO) {
        Integer sessionUserId = requiredSessionUserId();
        return Resp.ok(tripService.createPlan(sessionUserId, reqDTO));
    }

    @GetMapping
    public ResponseEntity<Resp<TripResponse.ListPageDTO>> getPlanList(
            @RequestParam(name = "category", defaultValue = "result") String category,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        Integer sessionUserId = requiredSessionUserId();
        return Resp.ok(tripService.getPlanList(sessionUserId, category, page));
    }

    @PostMapping("/{planId}/places")
    public ResponseEntity<Resp<TripResponse.PlaceAddedDTO>> addPlace(
            @PathVariable(name = "planId") Integer planId,
            @Valid @RequestBody TripRequest.AddPlaceDTO reqDTO) {
        Integer sessionUserId = requiredSessionUserId();
        return Resp.ok(tripService.addPlace(sessionUserId, planId, reqDTO));
    }

    @PostMapping("/{planId}/places/bulk")
    public ResponseEntity<Resp<Void>> addPlaces(
            @PathVariable(name = "planId") Integer planId,
            @RequestBody TripRequest.AddPlacesDTO reqDTO) {
        Integer sessionUserId = requiredSessionUserId();
        tripService.addPlacesToPlan(sessionUserId, planId, reqDTO);
        return Resp.ok(null);
    }

    private Integer requiredSessionUserId() {
        return SessionUsers.requireUserId(session);
    }
}
