package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.booking.Booking;
import com.example.travel_platform.booking.BookingRepository;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MypageService {

    private static final int UPCOMING_BOOKING_LIMIT = 2;
    private static final int UPCOMING_TRIP_PLAN_LIMIT = 2;

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;

    public MypageResponse.PageDTO getMainPage(Integer sessionUserId) {
        User user = findUser(sessionUserId);
        List<MypageResponse.BookingCardDTO> bookings = loadUpcomingBookings(sessionUserId);
        List<MypageResponse.PlanCardDTO> tripPlans = loadUpcomingTripPlans(sessionUserId);
        return createMainPage(user, bookings, tripPlans);
    }

    private List<MypageResponse.BookingCardDTO> loadUpcomingBookings(Integer sessionUserId) {
        return findUpcomingBookings(sessionUserId).stream()
                .map(booking -> MypageResponse.BookingCardDTO.from(booking))
                .collect(Collectors.toList());
    }

    private List<MypageResponse.PlanCardDTO> loadUpcomingTripPlans(Integer sessionUserId) {
        return findUpcomingTripPlans(sessionUserId).stream()
                .map(tripPlan -> MypageResponse.PlanCardDTO.from(tripPlan))
                .collect(Collectors.toList());
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

    private MypageResponse.PageDTO createMainPage(
            User user,
            List<MypageResponse.BookingCardDTO> bookings,
            List<MypageResponse.PlanCardDTO> tripPlans) {
        return MypageResponse.PageDTO.of(MypageResponse.ProfileDTO.from(user), bookings, tripPlans);
    }

    private List<Booking> findUpcomingBookings(Integer sessionUserId) {
        return bookingRepository.findByUser_IdAndCheckInGreaterThanEqualOrderByCheckInAscIdAsc(
                sessionUserId,
                LocalDate.now(),
                PageRequest.of(0, UPCOMING_BOOKING_LIMIT));
    }

    private List<TripPlan> findUpcomingTripPlans(Integer sessionUserId) {
        LocalDate inclusiveToday = LocalDate.now().minusDays(1);

        // TripRepository 조회 조건이 startDate > 기준일이므로 오늘 출발 계획도 포함되게 하루를 보정한다.
        return tripRepository.findUpcomingPlanListByUserId(sessionUserId, inclusiveToday, 0, UPCOMING_TRIP_PLAN_LIMIT);
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
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception400("사용자 정보를 찾을 수 없습니다."));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
