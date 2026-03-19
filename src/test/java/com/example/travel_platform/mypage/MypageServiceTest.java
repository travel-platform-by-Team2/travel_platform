package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MypageServiceTest {

    @Autowired
    private MypageService mypageService;

    @Test
    void getMainPage_loadsUpcomingBookingAndTripLinks() {
        MypageResponse.PageDTO page = mypageService.getMainPage(1);

        assertEquals("ssar", page.getUser().getUsername());
        assertTrue(page.getUser().isWithdrawAllowed());

        assertTrue(page.isHasBookings());
        assertEquals(1, page.getBookings().size());
        assertEquals("제주 오션뷰 호텔", page.getBookings().get(0).getLodgingName());
        assertEquals("/mypage/bookings/1", page.getBookings().get(0).getDetailLink());
        assertEquals("2026.04.10 - 2026.04.12", page.getBookings().get(0).getDateRangeLabel());

        assertTrue(page.isHasTripPlans());
        assertEquals(1, page.getTripPlans().size());
        assertEquals("제주 2박 3일", page.getTripPlans().get(0).getTitle());
        assertEquals("/trip/detail?id=1", page.getTripPlans().get(0).getDetailLink());
        assertEquals("2026.04.10 - 2026.04.12", page.getTripPlans().get(0).getDateRangeLabel());
    }

    @Test
    void getMainPage_returnsEmptySectionsForUserWithoutUpcomingData() {
        MypageResponse.PageDTO page = mypageService.getMainPage(3);

        assertEquals("admin", page.getUser().getUsername());
        assertFalse(page.getUser().isWithdrawAllowed());
        assertFalse(page.isHasBookings());
        assertTrue(page.getBookings().isEmpty());
        assertFalse(page.isHasTripPlans());
        assertTrue(page.getTripPlans().isEmpty());
    }
}
