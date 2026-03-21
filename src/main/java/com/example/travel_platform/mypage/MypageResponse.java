package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.travel_platform.user.User;

import lombok.Builder;
import lombok.Data;

public class MypageResponse {

    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Data
    @Builder
    public static class MainPageDTO {
        private ProfileViewDTO profile;
        private BookingSummarySectionDTO bookingSection;
        private TripPlanSummarySectionDTO tripPlanSection;
        private String passwordError;
        private boolean passwordModalOpen;
        private String withdrawError;
        private boolean withdrawModalOpen;
        private String passwordSuccessMessage;

        public static MainPageDTO createMainPage(
                ProfileViewDTO profile,
                BookingSummarySectionDTO bookingSection,
                TripPlanSummarySectionDTO tripPlanSection) {
            return MainPageDTO.builder()
                    .profile(profile)
                    .bookingSection(bookingSection)
                    .tripPlanSection(tripPlanSection)
                    .passwordError(null)
                    .passwordModalOpen(false)
                    .withdrawError(null)
                    .withdrawModalOpen(false)
                    .passwordSuccessMessage(null)
                    .build();
        }

        public MainPageDTO openPasswordModal(String errorMessage) {
            this.passwordError = normalize(errorMessage);
            this.passwordModalOpen = true;
            this.withdrawError = null;
            this.withdrawModalOpen = false;
            return this;
        }

        public MainPageDTO openWithdrawModal(String errorMessage) {
            this.passwordError = null;
            this.passwordModalOpen = false;
            this.withdrawError = normalize(errorMessage);
            this.withdrawModalOpen = true;
            return this;
        }

        public MainPageDTO withPasswordSuccess(String message) {
            this.passwordSuccessMessage = normalize(message);
            return this;
        }
    }

    @Data
    @Builder
    public static class ProfileViewDTO {
        private Integer id;
        private String username;
        private String email;
        private boolean withdrawAllowed;

        public static ProfileViewDTO fromUserEntity(User user) {
            return ProfileViewDTO.builder()
                    .id(user.getId())
                    .username(normalize(user.getUsername()))
                    .email(normalize(user.getEmail()))
                    .withdrawAllowed(!user.isAdmin())
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingSummarySectionDTO {
        private boolean hasItems;
        private List<BookingSummaryCardDTO> items;

        public static BookingSummarySectionDTO createBookingSection(List<BookingSummaryCardDTO> items) {
            return BookingSummarySectionDTO.builder()
                    .hasItems(!items.isEmpty())
                    .items(items)
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingSummaryCardDTO {
        private Integer id;
        private String lodgingName;
        private String dateRangeLabel;
        private String detailLink;

        public static BookingSummaryCardDTO createBookingSummaryCard(
                Integer bookingId,
                String lodgingName,
                LocalDate checkIn,
                LocalDate checkOut) {
            return BookingSummaryCardDTO.builder()
                    .id(bookingId)
                    .lodgingName(normalize(lodgingName))
                    .dateRangeLabel(formatDateRange(checkIn, checkOut))
                    .detailLink("/mypage/bookings/" + bookingId)
                    .build();
        }
    }

    @Data
    @Builder
    public static class TripPlanSummarySectionDTO {
        private boolean hasItems;
        private List<TripPlanSummaryCardDTO> items;

        public static TripPlanSummarySectionDTO createTripPlanSection(List<TripPlanSummaryCardDTO> items) {
            return TripPlanSummarySectionDTO.builder()
                    .hasItems(!items.isEmpty())
                    .items(items)
                    .build();
        }
    }

    @Data
    @Builder
    public static class TripPlanSummaryCardDTO {
        private Integer id;
        private String title;
        private String dateRangeLabel;
        private String detailLink;

        public static TripPlanSummaryCardDTO createTripPlanSummaryCard(
                Integer planId,
                String title,
                LocalDate startDate,
                LocalDate endDate) {
            return TripPlanSummaryCardDTO.builder()
                    .id(planId)
                    .title(normalize(title))
                    .dateRangeLabel(formatDateRange(startDate, endDate))
                    .detailLink("/trip/detail?id=" + planId)
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingDetailPlaceholderPageDTO {
        private Integer bookingId;
        private String backLink;
        private String placeholderNotice;

        public static BookingDetailPlaceholderPageDTO createBookingDetailPlaceholderPage(Integer bookingId) {
            return BookingDetailPlaceholderPageDTO.builder()
                    .bookingId(bookingId)
                    .backLink("/mypage")
                    .placeholderNotice("?꾩옱 ?붾㈃? placeholder?대ŉ ?덉빟 ID留??곌껐???곹깭?낅땲??")
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
