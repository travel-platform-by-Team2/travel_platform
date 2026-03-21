package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MypageService {

    private static final int UPCOMING_BOOKING_LIMIT = 2;
    private static final int UPCOMING_TRIP_PLAN_LIMIT = 2;

    private final UserQueryRepository userQueryRepository;
    private final MypageBookingQueryRepository mypageBookingQueryRepository;
    private final MypageTripPlanQueryRepository mypageTripPlanQueryRepository;

    public MypageResponse.MainPageDTO getMainPage(Integer sessionUserId) {
        User user = findUser(sessionUserId);
        List<MypageResponse.BookingSummaryCardDTO> bookings = loadUpcomingBookings(sessionUserId);
        List<MypageResponse.TripPlanSummaryCardDTO> tripPlans = loadUpcomingTripPlans(sessionUserId);
        return createMainPage(user, bookings, tripPlans);
    }

    public MypageResponse.BookingDetailPlaceholderPageDTO getBookingDetailPage(Integer sessionUserId, Integer bookingId) {
        findUser(sessionUserId);
        requireOwnedBooking(sessionUserId, bookingId);
        return MypageResponse.BookingDetailPlaceholderPageDTO.createBookingDetailPlaceholderPage(bookingId);
    }

    @Transactional
    public void changePassword(Integer sessionUserId, MypageRequest.ChangePasswordDTO reqDTO) {
        User user = findUser(sessionUserId);

        String currentPassword = normalize(reqDTO.getCurrentPassword());
        String newPassword = normalize(reqDTO.getNewPassword());
        String newPasswordConfirm = normalize(reqDTO.getNewPasswordConfirm());

        validateCurrentPassword(user, currentPassword);
        validateNewPasswordConfirm(newPassword, newPasswordConfirm);

        user.changePassword(newPassword);
    }

    private List<MypageResponse.BookingSummaryCardDTO> loadUpcomingBookings(Integer sessionUserId) {
        return mypageBookingQueryRepository.findUpcomingBookingSummaryRows(
                sessionUserId,
                LocalDate.now(),
                UPCOMING_BOOKING_LIMIT).stream()
                .map(this::createBookingSummaryCard)
                .collect(Collectors.toList());
    }

    private List<MypageResponse.TripPlanSummaryCardDTO> loadUpcomingTripPlans(Integer sessionUserId) {
        return mypageTripPlanQueryRepository.findUpcomingTripPlanSummaryRows(
                sessionUserId,
                LocalDate.now().minusDays(1),
                UPCOMING_TRIP_PLAN_LIMIT).stream()
                .map(this::createTripPlanSummaryCard)
                .collect(Collectors.toList());
    }

    private MypageResponse.MainPageDTO createMainPage(
            User user,
            List<MypageResponse.BookingSummaryCardDTO> bookings,
            List<MypageResponse.TripPlanSummaryCardDTO> tripPlans) {
        return MypageResponse.MainPageDTO.createMainPage(
                MypageResponse.ProfileViewDTO.fromUserEntity(user),
                MypageResponse.BookingSummarySectionDTO.createBookingSection(bookings),
                MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(tripPlans));
    }

    private MypageResponse.BookingSummaryCardDTO createBookingSummaryCard(MypageBookingSummaryRow row) {
        return MypageResponse.BookingSummaryCardDTO.createBookingSummaryCard(
                row.bookingId(),
                row.lodgingName(),
                row.checkIn(),
                row.checkOut());
    }

    private MypageResponse.TripPlanSummaryCardDTO createTripPlanSummaryCard(MypageTripPlanSummaryRow row) {
        return MypageResponse.TripPlanSummaryCardDTO.createTripPlanSummaryCard(
                row.planId(),
                row.title(),
                row.startDate(),
                row.endDate());
    }

    private void requireOwnedBooking(Integer sessionUserId, Integer bookingId) {
        if (!mypageBookingQueryRepository.existsOwnedBooking(sessionUserId, bookingId)) {
            throw new Exception404("예약 정보를 찾을 수 없습니다.");
        }
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (!normalize(user.getPassword()).equals(currentPassword)) {
            throw new Exception400("?꾩옱 鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎.");
        }
    }

    private void validateNewPasswordConfirm(String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new Exception400("??鍮꾨?踰덊샇? ?뺤씤媛믪씠 ?쇱튂?섏? ?딆뒿?덈떎.");
        }
    }

    private User findUser(Integer sessionUserId) {
        return userQueryRepository.findUser(sessionUserId)
                .orElseThrow(() -> new Exception400("?ъ슜???뺣낫瑜?李얠쓣 ???놁뒿?덈떎."));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
