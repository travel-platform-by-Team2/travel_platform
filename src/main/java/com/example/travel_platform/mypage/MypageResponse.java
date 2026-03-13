package com.example.travel_platform.mypage;

import java.util.List;

import lombok.Builder;
import lombok.Data;

public class MypageResponse {

    @Data
    @Builder
    public static class MainDTO {
        private ProfileDTO user;
        private boolean hasBookings;
        private List<BookingCardDTO> bookings;
        private boolean hasTripPlans;
        private List<PlanCardDTO> tripPlans;
    }

    @Data
    @Builder
    public static class ProfileDTO {
        private Integer id;
        private String username;
        private String email;
    }

    @Data
    @Builder
    public static class BookingCardDTO {
        private Integer id;
        private String lodgingName;
        private String dateRangeLabel;
        private String detailLink;
    }

    @Data
    @Builder
    public static class PlanCardDTO {
        private Integer id;
        private String title;
        private String dateRangeLabel;
    }
}
