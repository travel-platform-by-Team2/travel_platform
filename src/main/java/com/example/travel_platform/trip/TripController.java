package com.example.travel_platform.trip;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TripController {

    @GetMapping("/trip-list")
    public String tripListPage() {
        return "pages/trip-list";
    }

    @GetMapping("/trip-create")
    public String tripCreatePage() {
        return "pages/trip-create";
    }

    @GetMapping("/trip-detail/{planId}")
    public String tripDetailPage(@PathVariable Integer planId, Model model) {
        model.addAttribute("planId", planId);
        return "pages/trip-detail";
    }

    @GetMapping("/trip-add-place/{planId}")
    public String tripAddPlacePage(@PathVariable Integer planId, Model model) {
        model.addAttribute("planId", planId);
        return "pages/trip-add-place";
    }
}
