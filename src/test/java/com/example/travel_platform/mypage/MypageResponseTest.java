package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.travel_platform.booking.BookingResponse;

class MypageResponseTest {

    @Test
    void page() {
        MypageResponse.ProfileViewDTO profile = MypageResponse.ProfileViewDTO.builder()
                .username("ssar")
                .email("ssar@nate.com")
                .withdrawAllowed(true)
                .build();

        MypageResponse.MainPageDTO page = MypageResponse.MainPageDTO.createMainPage(
                profile,
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()));

        assertFalse(page.getBookingSection().isHasItems());
        assertEquals("/mypage/bookings", page.getBookingSection().getListLink());
        assertFalse(page.getTripPlanSection().isHasItems());
        assertFalse(page.isPasswordModalOpen());
        assertFalse(page.isWithdrawModalOpen());
    }

    @Test
    void pwd() {
        MypageResponse.MainPageDTO page = MypageResponse.MainPageDTO.createMainPage(
                MypageResponse.ProfileViewDTO.builder().build(),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()));

        page.openPasswordModal("현재 비밀번호가 일치하지 않습니다.");

        assertEquals("현재 비밀번호가 일치하지 않습니다.", page.getPasswordError());
        assertTrue(page.isPasswordModalOpen());
        assertEquals(null, page.getWithdrawError());
        assertFalse(page.isWithdrawModalOpen());
    }

    @Test
    void wd() {
        MypageResponse.MainPageDTO page = MypageResponse.MainPageDTO.createMainPage(
                MypageResponse.ProfileViewDTO.builder().build(),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()));

        page.openWithdrawModal("관리자 계정은 탈퇴할 수 없습니다.");

        assertEquals("관리자 계정은 탈퇴할 수 없습니다.", page.getWithdrawError());
        assertTrue(page.isWithdrawModalOpen());
        assertEquals(null, page.getPasswordError());
        assertFalse(page.isPasswordModalOpen());
    }

    @Test
    void ok() {
        MypageResponse.MainPageDTO page = MypageResponse.MainPageDTO.createMainPage(
                MypageResponse.ProfileViewDTO.builder().build(),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(List.of()),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(List.of()));

        page.withPasswordSuccess("비밀번호가 변경되었습니다.");

        assertEquals("비밀번호가 변경되었습니다.", page.getPasswordSuccessMessage());
    }

    @Test
    void listView() {
        BookingResponse.BookingSummaryDTO booking = BookingResponse.BookingSummaryDTO.builder()
                .id(21)
                .lodgingName("제주 오션뷰 호텔")
                .location("제주 제주시")
                .imageUrl("https://example.com/image.jpg")
                .checkIn(LocalDate.of(2026, 4, 10))
                .checkOut(LocalDate.of(2026, 4, 12))
                .statusCode("booked")
                .totalPriceText("330,400원")
                .build();

        MypageResponse.BookingListCardDTO card = MypageResponse.BookingListCardDTO.fromBookingSummary(
                booking,
                LocalDate.of(2026, 3, 21));
        MypageResponse.BookingListPageDTO page = MypageResponse.BookingListPageDTO.createBookingListPage(
                BookingCategory.UPCOMING,
                true);

        assertEquals("/mypage/bookings/21", card.getDetailLink());
        assertTrue(card.isUpcoming());
        assertFalse(card.isCompleted());
        assertFalse(card.isCancelled());
        assertTrue(page.isHasItems());
        assertTrue(page.isUpcomingSelected());
        assertFalse(page.isAllSelected());
    }

    @Test
    void detail() {
        MypageResponse.BookingDetailPageDTO page = MypageResponse.BookingDetailPageDTO.fromBookingDetail(
                BookingResponse.BookingDetailDTO.builder()
                        .id(21)
                        .tripPlanId(7)
                        .lodgingName("제주 오션뷰 호텔")
                        .roomName("오션뷰 스탠다드")
                        .location("제주")
                        .imageUrl("https://example.com/image.jpg")
                        .checkIn(LocalDate.of(2026, 4, 10))
                        .checkOut(LocalDate.of(2026, 4, 12))
                        .guestCount(2)
                        .pricePerNight(280000)
                        .taxAndServiceFee(50400)
                        .totalPriceText("330,400원")
                        .statusCode("booked")
                        .statusLabel("예약 확정")
                        .createdAt(LocalDateTime.of(2026, 3, 21, 10, 0))
                        .canCancel(true)
                        .build());

        assertEquals(21, page.getBookingId());
        assertEquals("BK-000021", page.getBookingNumberText());
        assertEquals("/mypage/bookings", page.getBookingListLink());
        assertEquals("/mypage", page.getMypageLink());
        assertEquals("예약 확정", page.getStatusLabel());
        assertEquals("성인 2명", page.getGuestCountLabel());
        assertEquals("330,400원", page.getTotalPriceText());
        assertEquals("/api/bookings/21", page.getCancelApiUrl());
        assertTrue(page.isHasTripPlanLink());
        assertFalse(page.isCancelled());
    }
}
