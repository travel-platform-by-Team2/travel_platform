package com.example.travel_platform.trip;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/trip")
@Controller
public class TripController {

    @GetMapping()
    public String tripListPage() {
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
}
