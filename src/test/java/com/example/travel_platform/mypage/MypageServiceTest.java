package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.booking.BookingService;

@SpringBootTest
class MypageServiceTest {

    @Autowired
    private MypageService mypageService;

    @Autowired
    private BookingService bookingService;

    @Test
    void main() {
        MypageResponse.MainPageDTO page = mypageService.getMainPage(1);

        assertEquals("ssar", page.getProfile().getUsername());
        assertTrue(page.getProfile().isWithdrawAllowed());

        assertTrue(page.getBookingSection().isHasItems());
        assertEquals("/mypage/bookings", page.getBookingSection().getListLink());
        assertEquals(1, page.getBookingSection().getItems().size());
        assertEquals("/mypage/bookings/1", page.getBookingSection().getItems().get(0).getDetailLink());
        assertEquals("2026.04.10 - 2026.04.12", page.getBookingSection().getItems().get(0).getDateRangeLabel());

        assertTrue(page.getTripPlanSection().isHasItems());
        assertEquals(1, page.getTripPlanSection().getItems().size());
        assertEquals("/trip/detail?id=1", page.getTripPlanSection().getItems().get(0).getDetailLink());
        assertEquals("2026.04.10 - 2026.04.12", page.getTripPlanSection().getItems().get(0).getDateRangeLabel());
    }

    @Test
    void empty() {
        MypageResponse.MainPageDTO page = mypageService.getMainPage(3);

        assertEquals("admin", page.getProfile().getUsername());
        assertFalse(page.getProfile().isWithdrawAllowed());
        assertFalse(page.getBookingSection().isHasItems());
        assertTrue(page.getBookingSection().getItems().isEmpty());
        assertFalse(page.getTripPlanSection().isHasItems());
        assertTrue(page.getTripPlanSection().getItems().isEmpty());
    }

    @Test
    void listAll() {
        MypageResponse.BookingListViewDTO view = mypageService.getBookingListView(1, null);

        assertTrue(view.getPage().isHasItems());
        assertEquals("예약 내역", view.getPage().getTitle());
        assertTrue(view.getPage().isAllSelected());
        assertFalse(view.getItems().isEmpty());
        assertEquals("/mypage/bookings/1", view.getItems().get(0).getDetailLink());
    }

    @Test
    @Transactional
    void listCancelled() {
        bookingService.cancelBooking(1, 1);

        MypageResponse.BookingListViewDTO view = mypageService.getBookingListView(1, "cancelled");

        assertTrue(view.getPage().isHasItems());
        assertTrue(view.getPage().isCancelledSelected());
        assertEquals(1, view.getItems().size());
        assertTrue(view.getItems().get(0).isCancelled());
    }

    @Test
    void bookingDetail() {
        MypageResponse.BookingDetailPageDTO page = mypageService.getBookingDetailPage(1, 1);

        assertEquals(1, page.getBookingId());
        assertEquals("/mypage/bookings", page.getBookingListLink());
        assertEquals("/mypage", page.getMypageLink());
        assertEquals("예약 확정", page.getStatusLabel());
        assertEquals("제주 오션뷰 호텔", page.getLodgingName());
        assertEquals("오션뷰 스탠다드", page.getRoomName());
        assertEquals("제주", page.getLocation());
        assertEquals("2026.04.10 - 2026.04.12 (2박)", page.getStayScheduleLabel());
        assertEquals("성인 2명", page.getGuestCountLabel());
        assertEquals("330,400원", page.getTotalPriceText());
        assertTrue(page.isCanCancel());
    }

    @Test
    @Transactional
    void mainSkipCancelled() {
        bookingService.cancelBooking(1, 1);

        MypageResponse.MainPageDTO page = mypageService.getMainPage(1);

        assertFalse(page.getBookingSection().isHasItems());
        assertTrue(page.getBookingSection().getItems().isEmpty());
    }

    @Test
    void booking404() {
        assertThrows(Exception404.class, () -> mypageService.getBookingDetailPage(1, 999));
    }
}
