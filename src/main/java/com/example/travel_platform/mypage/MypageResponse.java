package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.travel_platform.booking.Booking;
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
        private String passwordError;
        private boolean passwordModalOpen;
        private String withdrawError;
        private boolean withdrawModalOpen;
        private String passwordSuccessMessage;

        public static PageDTO of(ProfileDTO user, List<BookingCardDTO> bookings, List<PlanCardDTO> tripPlans) {
            return PageDTO.builder()
                    .user(user)
                    .hasBookings(!bookings.isEmpty())
                    .bookings(bookings)
                    .hasTripPlans(!tripPlans.isEmpty())
                    .tripPlans(tripPlans)
                    .passwordError(null)
                    .passwordModalOpen(false)
                    .withdrawError(null)
                    .withdrawModalOpen(false)
                    .passwordSuccessMessage(null)
                    .build();
        }

        public PageDTO openPasswordModal(String errorMessage) {
            this.passwordError = normalize(errorMessage);
            this.passwordModalOpen = true;
            this.withdrawError = null;
            this.withdrawModalOpen = false;
            return this;
        }

        public PageDTO openWithdrawModal(String errorMessage) {
            this.passwordError = null;
            this.passwordModalOpen = false;
            this.withdrawError = normalize(errorMessage);
            this.withdrawModalOpen = true;
            return this;
        }

        public PageDTO withPasswordSuccess(String message) {
            this.passwordSuccessMessage = normalize(message);
            return this;
        }
    }

    @Data
    @Builder
    public static class ProfileDTO {
        private Integer id;
        private String username;
        private String email;
        private boolean withdrawAllowed;

        public static ProfileDTO from(User user) {
            return ProfileDTO.builder()
                    .id(user.getId())
                    .username(normalize(user.getUsername()))
                    .email(normalize(user.getEmail()))
                    .withdrawAllowed(!user.isAdmin())
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

        public static BookingCardDTO from(Booking booking) {
            return BookingCardDTO.builder()
                    .id(booking.getId())
                    .lodgingName(normalize(booking.getLodgingName()))
                    .dateRangeLabel(formatDateRange(booking.getCheckIn(), booking.getCheckOut()))
                    .detailLink("/mypage/bookings/" + booking.getId())
                    .build();
        }
    }

    @Data
    @Builder
    public static class PlanCardDTO {
        private Integer id;
        private String title;
        private String dateRangeLabel;
        private String detailLink;

        public static PlanCardDTO from(TripPlan tripPlan) {
            return PlanCardDTO.builder()
                    .id(tripPlan.getId())
                    .title(normalize(tripPlan.getTitle()))
                    .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                    .detailLink("/trip/detail?id=" + tripPlan.getId())
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingDetailPageDTO {
        private Integer bookingId;
        private String backLink;
        private String placeholderNotice;

        public static BookingDetailPageDTO of(Integer bookingId) {
            return BookingDetailPageDTO.builder()
                    .bookingId(bookingId)
                    .backLink("/mypage")
                    .placeholderNotice("현재 화면은 placeholder이며 예약 ID만 연결된 상태입니다.")
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
