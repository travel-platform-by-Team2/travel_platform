package com.example.travel_platform.trip;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripApiController {

    private final TripService tripService;

    @PostMapping
    public void createPlan(@RequestBody TripRequest.CreatePlanDTO reqDTO) {
        tripService.createPlan(1, reqDTO);
    }

    @GetMapping
    public Object getPlanList(@RequestParam(defaultValue = "result") String category) {
        return tripService.getPlanList(1, category);
    }

    @PostMapping("/{planId}/places")
    public void addPlace(@PathVariable Integer planId, @RequestBody TripRequest.AddPlaceDTO reqDTO) {
        tripService.addPlace(1, planId, reqDTO);
    }
}
