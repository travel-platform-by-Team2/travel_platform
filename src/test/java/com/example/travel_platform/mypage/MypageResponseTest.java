package com.example.travel_platform.mypage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MypageResponseTest {

    @Test
    void page() {
        MypageResponse.ProfileDTO user = MypageResponse.ProfileDTO.builder()
                .username("ssar")
                .email("ssar@nate.com")
                .withdrawAllowed(true)
                .build();

        MypageResponse.PageDTO page = MypageResponse.PageDTO.createMainPage(user, List.of(), List.of());

        assertFalse(page.isHasBookings());
        assertFalse(page.isHasTripPlans());
        assertFalse(page.isPasswordModalOpen());
        assertFalse(page.isWithdrawModalOpen());
    }

    @Test
    void pwd() {
        MypageResponse.PageDTO page = MypageResponse.PageDTO.createMainPage(
                MypageResponse.ProfileDTO.builder().build(),
                List.of(),
                List.of());

        page.openPasswordModal("현재 비밀번호가 일치하지 않습니다.");

        assertEquals("현재 비밀번호가 일치하지 않습니다.", page.getPasswordError());
        assertTrue(page.isPasswordModalOpen());
        assertEquals(null, page.getWithdrawError());
        assertFalse(page.isWithdrawModalOpen());
    }

    @Test
    void wd() {
        MypageResponse.PageDTO page = MypageResponse.PageDTO.createMainPage(
                MypageResponse.ProfileDTO.builder().build(),
                List.of(),
                List.of());

        page.openWithdrawModal("관리자 계정은 탈퇴할 수 없습니다.");

        assertEquals("관리자 계정은 탈퇴할 수 없습니다.", page.getWithdrawError());
        assertTrue(page.isWithdrawModalOpen());
        assertEquals(null, page.getPasswordError());
        assertFalse(page.isPasswordModalOpen());
    }

    @Test
    void ok() {
        MypageResponse.PageDTO page = MypageResponse.PageDTO.createMainPage(
                MypageResponse.ProfileDTO.builder().build(),
                List.of(),
                List.of());

        page.withPasswordSuccess("비밀번호가 변경되었습니다.");

        assertEquals("비밀번호가 변경되었습니다.", page.getPasswordSuccessMessage());
    }

    @Test
    void detail() {
        MypageResponse.BookingDetailPageDTO page = MypageResponse.BookingDetailPageDTO.createBookingDetailPage(21);

        assertEquals(21, page.getBookingId());
        assertEquals("/mypage", page.getBackLink());
        assertEquals("현재 화면은 placeholder이며 예약 ID만 연결된 상태입니다.", page.getPlaceholderNotice());
    }
}
