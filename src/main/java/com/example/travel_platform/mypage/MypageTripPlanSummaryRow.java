package com.example.travel_platform.mypage;

import java.time.LocalDate;

public record MypageTripPlanSummaryRow(
        Integer planId,
        String title,
        LocalDate startDate,
        LocalDate endDate) {
}
