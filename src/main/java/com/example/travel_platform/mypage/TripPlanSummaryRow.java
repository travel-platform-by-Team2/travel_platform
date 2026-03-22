package com.example.travel_platform.mypage;

import java.time.LocalDate;

public record TripPlanSummaryRow(
        Integer planId,
        String title,
        LocalDate startDate,
        LocalDate endDate) {
}
