package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    public MypageResponse.MainPageDTO getMainPage(Integer sessionUserId) {
        return createMainPage(sessionUserId, MainPageState.DEFAULT, null);
    }

    public MypageResponse.MainPageDTO getPasswordSuccessMainPage(Integer sessionUserId, String message) {
        return createMainPage(sessionUserId, MainPageState.PASSWORD_SUCCESS, message);
    }

    public MypageResponse.MainPageDTO getPasswordFailureMainPage(Integer sessionUserId, String errorMessage) {
        return createMainPage(sessionUserId, MainPageState.PASSWORD_FAILURE, errorMessage);
    }

    public MypageResponse.MainPageDTO getWithdrawFailureMainPage(Integer sessionUserId, String errorMessage) {
        return createMainPage(sessionUserId, MainPageState.WITHDRAW_FAILURE, errorMessage);
    }

    private MypageResponse.MainPageDTO createMainPage(Integer sessionUserId, MainPageState state, String message) {
        User user = findUser(sessionUserId);
        List<MypageResponse.BookingSummaryCardDTO> bookings = loadUpcomingBookings(sessionUserId);
        List<MypageResponse.TripPlanSummaryCardDTO> tripPlans = loadUpcomingTripPlans(sessionUserId);
        return createMainPage(user, bookings, tripPlans, state, message);
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
        validateNewPasswordChanged(currentPassword, newPassword);
        validateNewPasswordConfirm(newPassword, newPasswordConfirm);

        user.changePassword(passwordEncoder.encode(newPassword));
    }

    private List<MypageResponse.BookingSummaryCardDTO> loadUpcomingBookings(Integer sessionUserId) {
        List<BookingSummaryRow> rows = mypageQueryRepository.findUpcomingBookingRows(
                sessionUserId,
                LocalDate.now(),
                UPCOMING_BOOKING_LIMIT);
        List<MypageResponse.BookingSummaryCardDTO> cards = new java.util.ArrayList<>();
        for (BookingSummaryRow row : rows) {
            cards.add(createBookingSummaryCard(row));
        }
        return cards;
    }

    private List<MypageResponse.TripPlanSummaryCardDTO> loadUpcomingTripPlans(Integer sessionUserId) {
        List<TripPlanSummaryRow> rows = mypageQueryRepository.findUpcomingTripPlanRows(
                sessionUserId,
                LocalDate.now().minusDays(1),
                UPCOMING_TRIP_PLAN_LIMIT);
        List<MypageResponse.TripPlanSummaryCardDTO> cards = new java.util.ArrayList<>();
        for (TripPlanSummaryRow row : rows) {
            cards.add(createTripPlanSummaryCard(row));
        }
        return cards;
    }

    private MypageResponse.MainPageDTO createMainPage(
            User user,
            List<MypageResponse.BookingSummaryCardDTO> bookings,
            List<MypageResponse.TripPlanSummaryCardDTO> tripPlans,
            MainPageState state,
            String message) {
        MypageResponse.ProfileViewDTO profile = MypageResponse.ProfileViewDTO.fromUserEntity(user);
        MypageResponse.BookingSummarySectionDTO bookingSection = MypageResponse.BookingSummarySectionDTO.createBookingSection(bookings);
        MypageResponse.TripPlanSummarySectionDTO tripPlanSection = MypageResponse.TripPlanSummarySectionDTO.createTripPlanSection(tripPlans);

        return switch (state) {
            case PASSWORD_SUCCESS -> MypageResponse.MainPageDTO.createPasswordSuccessPage(
                    profile,
                    bookingSection,
                    tripPlanSection,
                    message);
            case PASSWORD_FAILURE -> MypageResponse.MainPageDTO.createPasswordFailurePage(
                    profile,
                    bookingSection,
                    tripPlanSection,
                    message);
            case WITHDRAW_FAILURE -> MypageResponse.MainPageDTO.createWithdrawFailurePage(
                    profile,
                    bookingSection,
                    tripPlanSection,
                    message);
            case DEFAULT -> MypageResponse.MainPageDTO.createMainPage(
                    profile,
                    bookingSection,
                    tripPlanSection);
        };
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
        if (!passwordEncoder.matches(currentPassword, normalize(user.getPassword()))) {
            throw new Exception400("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validateNewPasswordConfirm(String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new Exception400("새 비밀번호와 확인값이 일치하지 않습니다.");
        }
    }

    private void validateNewPasswordChanged(String currentPassword, String newPassword) {
        if (currentPassword.equals(newPassword)) {
            throw new Exception400("새 비밀번호는 현재 비밀번호와 같을 수 없습니다.");
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

    private enum MainPageState {
        DEFAULT,
        PASSWORD_SUCCESS,
        PASSWORD_FAILURE,
        WITHDRAW_FAILURE
    }
}
