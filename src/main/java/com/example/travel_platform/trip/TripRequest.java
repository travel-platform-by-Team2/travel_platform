package com.example.travel_platform.trip;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

public class TripRequest {

    @Data
    public static class CreatePlanDTO {
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class AddPlaceDTO {
        private String placeName;
        private String address;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Integer dayOrder;
    }
}
