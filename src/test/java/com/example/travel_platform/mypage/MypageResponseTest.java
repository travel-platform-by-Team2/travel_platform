package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

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

        page.openPasswordModal("?꾩옱 鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.");

        assertEquals("?꾩옱 鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.", page.getPasswordError());
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

        page.openWithdrawModal("愿由ъ옄 怨꾩젙? ?덊눜?????놁뒿?덈떎.");

        assertEquals("愿由ъ옄 怨꾩젙? ?덊눜?????놁뒿?덈떎.", page.getWithdrawError());
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

        page.withPasswordSuccess("鍮꾨?踰덊샇媛 蹂寃쎈릺?덉뒿?덈떎.");

        assertEquals("鍮꾨?踰덊샇媛 蹂寃쎈릺?덉뒿?덈떎.", page.getPasswordSuccessMessage());
    }

    @Test
    void detail() {
        MypageResponse.BookingDetailPlaceholderPageDTO page = MypageResponse.BookingDetailPlaceholderPageDTO
                .createBookingDetailPlaceholderPage(21);

        assertEquals(21, page.getBookingId());
        assertEquals("/mypage", page.getBackLink());
        assertEquals("?꾩옱 ?붾㈃? placeholder?대ŉ ?덉빟 ID留??곌껐???곹깭?낅땲??", page.getPlaceholderNotice());
    }
}
