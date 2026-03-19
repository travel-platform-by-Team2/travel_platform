package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.user.User;

import lombok.Builder;
import lombok.Data;

public class MypageResponse {

    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Data
    @Builder
    public static class PageDTO {
        private ProfileDTO user;
        private boolean hasBookings;
        private List<BookingCardDTO> bookings;
        private boolean hasTripPlans;
        private List<PlanCardDTO> tripPlans;

        public static PageDTO of(ProfileDTO user, List<BookingCardDTO> bookings, List<PlanCardDTO> tripPlans) {
            return PageDTO.builder()
                    .user(user)
                    .hasBookings(!bookings.isEmpty())
                    .bookings(bookings)
                    .hasTripPlans(!tripPlans.isEmpty())
                    .tripPlans(tripPlans)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ProfileDTO {
        private Integer id;
        private String username;
        private String email;

        public static ProfileDTO from(User user) {
            return ProfileDTO.builder()
                    .id(user.getId())
                    .username(normalize(user.getUsername()))
                    .email(normalize(user.getEmail()))
                    .build();
        }
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

        public static PlanCardDTO from(TripPlan tripPlan) {
            return PlanCardDTO.builder()
                    .id(tripPlan.getId())
                    .title(normalize(tripPlan.getTitle()))
                    .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .build();
        }
    }

    private static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return startDate.format(DATE_LABEL_FORMATTER) + " - " + endDate.format(DATE_LABEL_FORMATTER);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
