package com.example.travel_platform.trip;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trip-plans")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public void createPlan(@Valid @RequestBody TripRequest.CreatePlanDTO reqDTO) {
        // TODO: 세션에서 사용자 식별값 추출
        tripService.createPlan(1, reqDTO);
    }

    @GetMapping
    public Object getPlanList() {
        // TODO: 세션 사용자 기준 목록 조회
        return tripService.getPlanList(1);
    }

    @GetMapping("/{planId}")
    public Object getPlanDetail(@PathVariable Integer planId) {
        // TODO: 세션 사용자 + planId 검증
        return tripService.getPlanDetail(1, planId);
    }

    @PostMapping("/{planId}/places")
    public void addPlace(@PathVariable Integer planId, @Valid @RequestBody TripRequest.AddPlaceDTO reqDTO) {
        // TODO: 세션 사용자 + planId 검증
        tripService.addPlace(1, planId, reqDTO);
    }
}

