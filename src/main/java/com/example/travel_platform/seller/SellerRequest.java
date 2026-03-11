package com.example.travel_platform.seller;

import lombok.Data;

public class SellerRequest {

    @Data
    public static class CreateProductDTO {
        private String name;
        private String category;
        private Integer price;
        private String status;
    }

    @Data
    public static class UpdateBookingStatusDTO {
        private String status;
        private String memo;
    }

    @Data
    public static class SettlementExportDTO {
        private String settlementMonth;
        private String fileFormat;
    }
}
