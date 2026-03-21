package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.example.travel_platform.booking.BookingResponse;
import com.example.travel_platform.user.User;

import lombok.Builder;
import lombok.Data;

public class MypageResponse {

    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter DATE_TIME_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

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
        private String listLink;
        private List<BookingSummaryCardDTO> items;

        public static BookingSummarySectionDTO createBookingSection(List<BookingSummaryCardDTO> items) {
            return BookingSummarySectionDTO.builder()
                    .hasItems(!items.isEmpty())
                    .listLink("/mypage/bookings")
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
    public static class BookingListViewDTO {
        private BookingListPageDTO page;
        private List<BookingListCardDTO> items;

        public static BookingListViewDTO createBookingListView(
                BookingListPageDTO page,
                List<BookingListCardDTO> items) {
            return BookingListViewDTO.builder()
                    .page(page)
                    .items(items)
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingListPageDTO {
        private String title;
        private String mypageLink;
        private boolean hasItems;
        private boolean allSelected;
        private boolean upcomingSelected;
        private boolean completedSelected;
        private boolean cancelledSelected;

        public static BookingListPageDTO createBookingListPage(
                BookingCategory selectedCategory,
                boolean hasItems) {
            return BookingListPageDTO.builder()
                    .title("예약 내역")
                    .mypageLink("/mypage")
                    .hasItems(hasItems)
                    .allSelected(selectedCategory == BookingCategory.ALL)
                    .upcomingSelected(selectedCategory == BookingCategory.UPCOMING)
                    .completedSelected(selectedCategory == BookingCategory.COMPLETED)
                    .cancelledSelected(selectedCategory == BookingCategory.CANCELLED)
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingListCardDTO {
        private Integer id;
        private String bookingNumberText;
        private String lodgingName;
        private String locationDateLabel;
        private String totalPriceText;
        private String detailLink;
        private String imageUrl;
        private boolean hasImage;
        private boolean upcoming;
        private boolean completed;
        private boolean cancelled;

        public static BookingListCardDTO fromBookingSummary(
                BookingResponse.BookingSummaryDTO booking,
                LocalDate today) {
            BookingCategory category = BookingCategory.fromBooking(booking, today);
            return BookingListCardDTO.builder()
                    .id(booking.getId())
                    .bookingNumberText(formatBookingNumber(booking.getId()))
                    .lodgingName(normalize(booking.getLodgingName()))
                    .locationDateLabel(formatLocationDateLabel(
                            booking.getLocation(),
                            booking.getCheckIn(),
                            booking.getCheckOut()))
                    .totalPriceText(normalize(booking.getTotalPriceText()))
                    .detailLink("/mypage/bookings/" + booking.getId())
                    .imageUrl(normalize(booking.getImageUrl()))
                    .hasImage(booking.getImageUrl() != null && !booking.getImageUrl().isBlank())
                    .upcoming(category == BookingCategory.UPCOMING)
                    .completed(category == BookingCategory.COMPLETED)
                    .cancelled(category == BookingCategory.CANCELLED)
                    .build();
        }
    }

    @Data
    @Builder
    public static class BookingDetailPageDTO {
        private Integer bookingId;
        private Integer tripPlanId;
        private String bookingNumberText;
        private String bookingListLink;
        private String mypageLink;
        private String statusLabel;
        private String bookingDateLabel;
        private String cancelledAtLabel;
        private String lodgingName;
        private String roomName;
        private String location;
        private String imageUrl;
        private boolean hasImage;
        private String stayScheduleLabel;
        private String guestCountLabel;
        private String roomPriceText;
        private String feeText;
        private String totalPriceText;
        private boolean hasTripPlanLink;
        private String tripPlanLink;
        private boolean canCancel;
        private String cancelApiUrl;
        private boolean cancelled;

        public static BookingDetailPageDTO fromBookingDetail(BookingResponse.BookingDetailDTO detail) {
            boolean cancelled = "cancelled".equals(detail.getStatusCode());
            return BookingDetailPageDTO.builder()
                    .bookingId(detail.getId())
                    .tripPlanId(detail.getTripPlanId())
                    .bookingNumberText(formatBookingNumber(detail.getId()))
                    .bookingListLink("/mypage/bookings")
                    .mypageLink("/mypage")
                    .statusLabel(normalize(detail.getStatusLabel()))
                    .bookingDateLabel(formatDateTime(detail.getCreatedAt()))
                    .cancelledAtLabel(formatDateTime(detail.getCancelledAt()))
                    .lodgingName(normalize(detail.getLodgingName()))
                    .roomName(normalize(detail.getRoomName()))
                    .location(normalize(detail.getLocation()))
                    .imageUrl(normalize(detail.getImageUrl()))
                    .hasImage(detail.getImageUrl() != null && !detail.getImageUrl().isBlank())
                    .stayScheduleLabel(formatStaySchedule(detail.getCheckIn(), detail.getCheckOut()))
                    .guestCountLabel(formatGuestCount(detail.getGuestCount()))
                    .roomPriceText(formatWon(detail.getPricePerNight()))
                    .feeText(formatWon(detail.getTaxAndServiceFee()))
                    .totalPriceText(normalize(detail.getTotalPriceText()))
                    .hasTripPlanLink(detail.getTripPlanId() != null)
                    .tripPlanLink(detail.getTripPlanId() == null ? "" : "/trip/detail?id=" + detail.getTripPlanId())
                    .canCancel(detail.isCanCancel())
                    .cancelApiUrl("/api/bookings/" + detail.getId())
                    .cancelled(cancelled)
                    .build();
        }
    }

    private static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return startDate.format(DATE_LABEL_FORMATTER) + " - " + endDate.format(DATE_LABEL_FORMATTER);
    }

    private static String formatLocationDateLabel(String location, LocalDate checkIn, LocalDate checkOut) {
        String staySchedule = formatStaySchedule(checkIn, checkOut);
        String normalizedLocation = normalize(location);
        if (normalizedLocation.isBlank()) {
            return staySchedule;
        }
        if (staySchedule.isBlank()) {
            return normalizedLocation;
        }
        return normalizedLocation + " | " + staySchedule;
    }

    private static String formatStaySchedule(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return "";
        }

        long nights = Math.max(1, ChronoUnit.DAYS.between(checkIn, checkOut));
        return checkIn.format(DATE_LABEL_FORMATTER)
                + " - "
                + checkOut.format(DATE_LABEL_FORMATTER)
                + " ("
                + nights
                + "박)";
    }

    private static String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DATE_TIME_LABEL_FORMATTER);
    }

    private static String formatGuestCount(Integer guestCount) {
        if (guestCount == null || guestCount <= 0) {
            return "";
        }
        return "성인 " + guestCount + "명";
    }

    private static String formatBookingNumber(Integer bookingId) {
        if (bookingId == null) {
            return "";
        }
        return "BK-" + String.format("%06d", bookingId);
    }

    private static String formatWon(Integer value) {
        if (value == null) {
            return "0원";
        }
        return String.format("%,d원", value);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
