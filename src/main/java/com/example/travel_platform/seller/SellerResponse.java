package com.example.travel_platform.seller;

import java.util.List;

import lombok.Builder;
import lombok.Data;

public class SellerResponse {

    @Data
    @Builder
    public static class DashboardOverviewDTO {
        private String monthlySalesText;
        private Integer activeBookingCount;
        private String ratingText;
        private Integer productCount;
    }

    @Data
    @Builder
    public static class ProductSummaryDTO {
        private Integer id;
        private String name;
        private String category;
        private String priceText;
        private String status;
    }

    @Data
    @Builder
    public static class ProductListDTO {
        private Integer totalCount;
        private List<ProductSummaryDTO> items;
    }

    @Data
    @Builder
    public static class BookingSummaryDTO {
        private String bookingCode;
        private String guestName;
        private String productName;
        private String stayText;
        private String status;
        private String priceText;
    }

    @Data
    @Builder
    public static class BookingListDTO {
        private Integer totalCount;
        private List<BookingSummaryDTO> items;
    }

    @Data
    @Builder
    public static class SettlementSummaryDTO {
        private String settlementMonth;
        private String grossSalesText;
        private String feeText;
        private String adjustmentText;
        private String payoutText;
        private String status;
    }

    @Data
    @Builder
    public static class SettlementListDTO {
        private Integer totalCount;
        private List<SettlementSummaryDTO> items;
    }
}
