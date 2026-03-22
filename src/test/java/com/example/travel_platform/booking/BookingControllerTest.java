package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.example.travel_platform._core.handler.ex.Exception400;
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
                "hotel",
                "room",
                "busan street 30",
                null,
                "2026-04-10",
                "2026-04-12",
                "adult 2",
                450000,
                100000,
                model);

        BookingResponse.CheckoutPageDTO page = (BookingResponse.CheckoutPageDTO) model.getAttribute("model");
        assertEquals("pages/booking-checkout", view);
        assertEquals("hotel", page.getLodgingName());
        assertEquals("room", page.getRoomName());
        assertEquals("busan", page.getRegionKey());
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
                "hotel",
                "suite",
                "busan",
                "https://image.test/hotel.jpg",
                "2026-05-01",
                "2026-05-02",
                "adult 3",
                -1,
                -1,
                model);

        BookingResponse.CheckoutPageDTO page = (BookingResponse.CheckoutPageDTO) model.getAttribute("model");
        assertEquals("pages/booking-checkout", view);
        assertEquals("ssar", page.getBookerName());
        assertEquals("ssar@nate.com", page.getBookerEmail());
        assertEquals("010-1111-2222", page.getBookerPhone());
        assertEquals("busan", page.getRegionKey());
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
                "hotel",
                "room",
                "",
                "",
                "adult 2",
                "2026-04-10",
                "2026-04-12",
                "",
                450000,
                200000,
                null,
                model);

        BookingResponse.CompletePageDTO page = (BookingResponse.CompletePageDTO) model.getAttribute("model");
        assertEquals("pages/booking-complete", view);
        assertEquals("busan", page.getRegionKey());
        assertEquals("2박", page.getNightsLabel());
        assertEquals("0원", page.getTotalPriceText());
        assertTrue(page.getCompleteImageUrl().startsWith("https://"));
        verifyNoInteractions(bookingService);
    }

    @Test
    void doneUser() {
        BookingService bookingService = mock(BookingService.class);
        MockHttpSession session = session(7);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, session);
        Model model = new ExtendedModelMap();

        String view = controller.completePage(
                "lotte",
                "premier",
                "seoul",
                "seoul",
                "adult 2",
                "2026-06-01",
                "2026-06-03",
                "1,250,000원",
                500000,
                250000,
                "https://image.test/complete.jpg",
                model);

        BookingResponse.CompletePageDTO page = (BookingResponse.CompletePageDTO) model.getAttribute("model");
        assertEquals("pages/booking-complete", view);
        assertEquals("seoul", page.getRegionKey());
        assertEquals("https://image.test/complete.jpg", page.getCompleteImageUrl());
        verify(bookingService).processBookingCompletion(
                eq(7),
                argThat(reqDTO -> reqDTO != null
                        && "lotte".equals(reqDTO.getLodgingName())
                        && "premier".equals(reqDTO.getRoomName())
                        && "seoul".equals(reqDTO.getRegionKey())
                        && "2026-06-01".equals(reqDTO.getCheckIn())
                        && "2026-06-03".equals(reqDTO.getCheckOut())
                        && "adult 2".equals(reqDTO.getGuests())
                        && Integer.valueOf(500000).equals(reqDTO.getPricePerNight())
                        && Integer.valueOf(250000).equals(reqDTO.getTaxAndServiceFee())
                        && "https://image.test/complete.jpg".equals(reqDTO.getImageUrl())));
    }

    @Test
    void coBadDate() {
        BookingService bookingService = mock(BookingService.class);
        BookingController controller = new BookingController("kakao-key", "tour-key", bookingService, new MockHttpSession());
        Model model = new ExtendedModelMap();

        Exception400 exception = assertThrows(
                Exception400.class,
                () -> controller.checkoutPage(
                        "hotel",
                        "room",
                        "busan address 30",
                        null,
                        "2026-04-10",
                        "bad-date",
                        "adult 2",
                        450000,
                        100000,
                        model));

        assertEquals("숙박 날짜 형식이 올바르지 않습니다.", exception.getMessage());
    }

    private MockHttpSession session(Integer userId) {
        MockHttpSession session = new MockHttpSession();
        SessionUsers.save(session, new SessionUser(userId, "ssar", "ssar@nate.com", "010-1111-2222", "USER"));
        return session;
    }
}
