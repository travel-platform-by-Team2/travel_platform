package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.travel_platform._core.handler.ex.Exception404;

@SpringBootTest
class MypageServiceTest {

    @Autowired
    private MypageService mypageService;

    @Test
    void main() {
        MypageResponse.MainPageDTO page = mypageService.getMainPage(1);

        assertEquals("ssar", page.getProfile().getUsername());
        assertTrue(page.getProfile().isWithdrawAllowed());

        assertTrue(page.getBookingSection().isHasItems());
        assertEquals(1, page.getBookingSection().getItems().size());
        assertEquals("제주 오션뷰 호텔", page.getBookingSection().getItems().get(0).getLodgingName());
        assertEquals("/mypage/bookings/1", page.getBookingSection().getItems().get(0).getDetailLink());
        assertEquals("2026.04.10 - 2026.04.12", page.getBookingSection().getItems().get(0).getDateRangeLabel());

        assertTrue(page.getTripPlanSection().isHasItems());
        assertEquals(1, page.getTripPlanSection().getItems().size());
        assertEquals("제주 2박 3일", page.getTripPlanSection().getItems().get(0).getTitle());
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
    void booking404() {
        assertThrows(Exception404.class, () -> mypageService.getBookingDetailPage(1, 999));
    }
}
