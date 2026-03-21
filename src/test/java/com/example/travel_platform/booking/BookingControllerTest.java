package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.example.travel_platform.user.SessionUser;
import com.example.travel_platform.user.SessionUsers;
import com.example.travel_platform.user.User;

class BookingControllerTest {

    @Test
    void map() {
        BookingService bookingService = mock(BookingService.class);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, new MockHttpSession());
        Model model = new ExtendedModelMap();

        String view = controller.detailMap(model);

        BookingResponse.MapDetailPageDTO page = (BookingResponse.MapDetailPageDTO) model.getAttribute("model");
        assertEquals("pages/map-detail", view);
        assertEquals("kakao-key", page.getKakaoMapAppKey());
        verifyNoInteractions(bookingService);
    }

    @Test
    void coGuest() {
        BookingService bookingService = mock(BookingService.class);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, new MockHttpSession());
        Model model = new ExtendedModelMap();

        String view = controller.checkoutPage(
                "시그니엘 부산",
                "디럭스 룸",
                "부산 해운대구 달맞이길 30",
                null,
                "2026-04-10",
                "2026-04-12",
                "성인 2명",
                450000,
                100000,
                model);

        BookingResponse.CheckoutPageDTO page = (BookingResponse.CheckoutPageDTO) model.getAttribute("model");
        assertEquals("pages/booking-checkout", view);
        assertEquals("시그니엘 부산", page.getLodgingName());
        assertEquals("디럭스 룸", page.getRoomName());
        assertEquals("busan", page.getRegionKey());
        assertEquals("부산", page.getRegionLabel());
        assertEquals("", page.getImageUrl());
        assertEquals("2박", page.getNightsLabel());
        assertEquals("450,000원", page.getRoomPriceText());
        assertEquals("900,000원", page.getRoomSubtotalText());
        assertEquals("200,000원", page.getFeeText());
        assertEquals("1,100,000원", page.getTotalPriceText());
        assertEquals("", page.getBookerName());
        assertEquals("", page.getBookerEmail());
        assertEquals("", page.getBookerPhone());
        verifyNoInteractions(bookingService);
    }

    @Test
    void coUser() {
        BookingService bookingService = mock(BookingService.class);
        MockHttpSession session = session(5);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, session);
        Model model = new ExtendedModelMap();
        User user = User.create("ssar", "1234", "ssar@nate.com", "010-1111-2222", "USER");

        when(bookingService.getUserById(5)).thenReturn(user);

        String view = controller.checkoutPage(
                "파라다이스 호텔",
                "오션 스위트",
                "부산 해운대구",
                "https://image.test/hotel.jpg",
                "2026-05-01",
                "2026-05-02",
                "성인 3명",
                -1,
                -1,
                model);

        BookingResponse.CheckoutPageDTO page = (BookingResponse.CheckoutPageDTO) model.getAttribute("model");
        assertEquals("pages/booking-checkout", view);
        assertEquals("ssar", page.getBookerName());
        assertEquals("ssar@nate.com", page.getBookerEmail());
        assertEquals("010-1111-2222", page.getBookerPhone());
        assertEquals("busan", page.getRegionKey());
        assertEquals("부산", page.getRegionLabel());
        assertEquals("350,000원", page.getRoomPriceText());
        assertEquals("105,000원", page.getFeeText());
        verify(bookingService).getUserById(5);
    }

    @Test
    void doneGuest() {
        BookingService bookingService = mock(BookingService.class);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, new MockHttpSession());
        Model model = new ExtendedModelMap();

        String view = controller.completePage(
                "시그니엘 부산",
                "디럭스 룸",
                "",
                "",
                "성인 2명",
                "2026-04-10",
                "2026-04-12",
                "",
                450000,
                200000,
                null,
                model);

        BookingResponse.CompletePageDTO page = (BookingResponse.CompletePageDTO) model.getAttribute("model");
        assertEquals("pages/booking-complete", view);
        assertEquals("부산", page.getRegion());
        assertEquals("busan", page.getRegionKey());
        assertEquals("2박", page.getNightsLabel());
        assertEquals("0원", page.getTotalPriceText());
        assertEquals("https://lh3.googleusercontent.com/aida-public/AB6AXuC-tNVV57D0EwHVcc8AGgHsqFcUf1oHeJUsCxZ-987Qnye2F7JO9sQyk8t_AWfw0W3RDx8bJWwNKOLLAFJe_IIC1x8Pdg3Q6_YzcyaKkC7GitmYoVQPK24H1H4ZGnJYOn_ihHy2Tp-8xS1yfeVoS0dIPgu3UwUeR3w16rvw0eJ-X49iGCKDq0ku2fbWdoYPv_RklQ4NrLhuBb5HSC1KdxB4_6rQkDx3n2Z8l1IsBQTL0F_C2wv7gApGTmObL4V1gUyPs9A2p3zThbw",
                page.getCompleteImageUrl());
        verifyNoInteractions(bookingService);
    }

    @Test
    void doneUser() {
        BookingService bookingService = mock(BookingService.class);
        MockHttpSession session = session(7);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, session);
        Model model = new ExtendedModelMap();

        String view = controller.completePage(
                "롯데 호텔",
                "프리미어 룸",
                "서울특별시 중구",
                "seoul",
                "성인 2명",
                "2026-06-01",
                "2026-06-03",
                "1,250,000원",
                500000,
                250000,
                "https://image.test/complete.jpg",
                model);

        BookingResponse.CompletePageDTO page = (BookingResponse.CompletePageDTO) model.getAttribute("model");
        assertEquals("pages/booking-complete", view);
        assertEquals("서울", page.getRegion());
        assertEquals("seoul", page.getRegionKey());
        assertEquals("https://image.test/complete.jpg", page.getCompleteImageUrl());
        verify(bookingService).processBookingCompletion(
                eq(7),
                argThat(reqDTO -> reqDTO != null
                        && "롯데 호텔".equals(reqDTO.getLodgingName())
                        && "프리미어 룸".equals(reqDTO.getRoomName())
                        && "seoul".equals(reqDTO.getRegionKey())
                        && "서울".equals(reqDTO.getLocation())
                        && "2026-06-01".equals(reqDTO.getCheckIn())
                        && "2026-06-03".equals(reqDTO.getCheckOut())
                        && "성인 2명".equals(reqDTO.getGuests())
                        && Integer.valueOf(500000).equals(reqDTO.getPricePerNight())
                        && Integer.valueOf(250000).equals(reqDTO.getTaxAndServiceFee())
                        && "https://image.test/complete.jpg".equals(reqDTO.getImageUrl())));
    }

    private MockHttpSession session(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }
}
