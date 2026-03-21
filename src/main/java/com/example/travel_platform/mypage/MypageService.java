package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.booking.BookingService;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MypageService {

    private static final int UPCOMING_BOOKING_LIMIT = 2;
    private static final int UPCOMING_TRIP_PLAN_LIMIT = 2;

    private final BookingService bookingService;
    private final UserQueryRepository userQueryRepository;
    private final MypageQueryRepository mypageQueryRepository;

    public MypageResponse.MainPageDTO getMainPage(Integer sessionUserId) {
        User user = findUser(sessionUserId);
        List<MypageResponse.BookingSummaryCardDTO> bookings = loadUpcomingBookings(sessionUserId);
        List<MypageResponse.TripPlanSummaryCardDTO> tripPlans = loadUpcomingTripPlans(sessionUserId);
        return createMainPage(user, bookings, tripPlans);
    }

    public MypageResponse.BookingListViewDTO getBookingListView(Integer sessionUserId, String categoryCode) {
        findUser(sessionUserId);

        LocalDate today = LocalDate.now();
        BookingCategory selectedCategory = BookingCategory.fromCode(categoryCode);
        List<MypageResponse.BookingListCardDTO> items = bookingService.getBookingList(sessionUserId).stream()
                .filter(booking -> selectedCategory.matches(booking, today))
                .map(booking -> MypageResponse.BookingListCardDTO.fromBookingSummary(booking, today))
                .toList();

        return MypageResponse.BookingListViewDTO.createBookingListView(
                MypageResponse.BookingListPageDTO.createBookingListPage(selectedCategory, !items.isEmpty()),
                items);
    }

    public MypageResponse.BookingDetailPageDTO getBookingDetailPage(Integer sessionUserId, Integer bookingId) {
        findUser(sessionUserId);
        return MypageResponse.BookingDetailPageDTO.fromBookingDetail(
                bookingService.getBookingDetail(sessionUserId, bookingId));
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
        return mypageQueryRepository.findUpcomingBookingRows(
                sessionUserId,
                LocalDate.now(),
                UPCOMING_BOOKING_LIMIT).stream()
                .map(this::createBookingSummaryCard)
                .toList();
    }

    private List<MypageResponse.TripPlanSummaryCardDTO> loadUpcomingTripPlans(Integer sessionUserId) {
        return mypageQueryRepository.findUpcomingTripPlanRows(
                sessionUserId,
                LocalDate.now().minusDays(1),
                UPCOMING_TRIP_PLAN_LIMIT).stream()
                .map(this::createTripPlanSummaryCard)
                .toList();
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

    private MypageResponse.BookingSummaryCardDTO createBookingSummaryCard(BookingSummaryRow row) {
        return MypageResponse.BookingSummaryCardDTO.createBookingSummaryCard(
                row.bookingId(),
                row.lodgingName(),
                row.checkIn(),
                row.checkOut());
    }

    private MypageResponse.TripPlanSummaryCardDTO createTripPlanSummaryCard(TripPlanSummaryRow row) {
        return MypageResponse.TripPlanSummaryCardDTO.createTripPlanSummaryCard(
                row.planId(),
                row.title(),
                row.startDate(),
                row.endDate());
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (!normalize(user.getPassword()).equals(currentPassword)) {
            throw new Exception400("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validateNewPasswordConfirm(String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new Exception400("새 비밀번호와 확인값이 일치하지 않습니다.");
        }
    }

    private User findUser(Integer sessionUserId) {
        return userQueryRepository.findUser(sessionUserId)
                .orElseThrow(() -> new Exception400("사용자 정보를 찾을 수 없습니다."));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
