package com.example.travel_platform.seller;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SellerService {

    public SellerResponse.DashboardOverviewDTO getDashboardOverview() {
        return SellerResponse.DashboardOverviewDTO.builder()
                .monthlySalesText("12,450,000원")
                .activeBookingCount(48)
                .ratingText("4.8 / 5.0")
                .productCount(12)
                .build();
    }

    public SellerResponse.ProductListDTO getProductList() {
        return SellerResponse.ProductListDTO.builder()
                .totalCount(3)
                .items(List.of(
                        SellerResponse.ProductSummaryDTO.builder()
                                .id(124)
                                .name("디럭스 오션뷰 룸")
                                .category("객실/숙박")
                                .priceText("250,000원")
                                .status("판매중")
                                .build(),
                        SellerResponse.ProductSummaryDTO.builder()
                                .id(123)
                                .name("프리미엄 가든 룸")
                                .category("객실/숙박")
                                .priceText("180,000원")
                                .status("노출중")
                                .build(),
                        SellerResponse.ProductSummaryDTO.builder()
                                .id(122)
                                .name("그랜드 패밀리 스위트")
                                .category("객실/숙박")
                                .priceText("450,000원")
                                .status("판매대기")
                                .build()))
                .build();
    }

    public SellerResponse.BookingListDTO getBookingList() {
        return SellerResponse.BookingListDTO.builder()
                .totalCount(4)
                .items(List.of(
                        SellerResponse.BookingSummaryDTO.builder()
                                .bookingCode("#BK-20260321")
                                .guestName("김철수")
                                .productName("프리미엄 오션뷰 스위트")
                                .stayText("2026.03.15 ~ 2026.03.17 (2박)")
                                .status("예약확정")
                                .priceText("450,000원")
                                .build(),
                        SellerResponse.BookingSummaryDTO.builder()
                                .bookingCode("#BK-20260318")
                                .guestName("이영희")
                                .productName("디럭스 더블룸")
                                .stayText("2026.03.10 ~ 2026.03.11 (1박)")
                                .status("이용완료")
                                .priceText("210,000원")
                                .build()))
                .build();
    }

    public SellerResponse.SettlementListDTO getSettlementList() {
        return SellerResponse.SettlementListDTO.builder()
                .totalCount(3)
                .items(List.of(
                        SellerResponse.SettlementSummaryDTO.builder()
                                .settlementMonth("2026년 03월")
                                .grossSalesText("5,000,000원")
                                .feeText("-500,000원")
                                .adjustmentText("-250,000원")
                                .payoutText("4,250,000원")
                                .status("정산예정")
                                .build(),
                        SellerResponse.SettlementSummaryDTO.builder()
                                .settlementMonth("2026년 02월")
                                .grossSalesText("4,500,000원")
                                .feeText("-450,000원")
                                .adjustmentText("-160,000원")
                                .payoutText("3,890,000원")
                                .status("지급완료")
                                .build()))
                .build();
    }
}
