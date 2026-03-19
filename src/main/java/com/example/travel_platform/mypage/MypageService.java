package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.booking.BookingRepository;
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
        MypageResponse.ProfileDTO user = loadProfile(sessionUserId);
        List<MypageResponse.BookingCardDTO> bookings = loadUpcomingBookings(sessionUserId);
        List<MypageResponse.PlanCardDTO> tripPlans = loadUpcomingTripPlans(sessionUserId);

        return MypageResponse.PageDTO.of(user, bookings, tripPlans);
    }

    private List<MypageResponse.BookingCardDTO> loadUpcomingBookings(Integer sessionUserId) {
        LocalDate today = LocalDate.now();

        return bookingRepository.findByUser_IdAndCheckInGreaterThanEqualOrderByCheckInAscIdAsc(
                        sessionUserId,
                        today,
                        PageRequest.of(0, UPCOMING_BOOKING_LIMIT))
                .stream()
                .map(booking -> MypageResponse.BookingCardDTO.from(booking))
                .collect(Collectors.toList());
    }

    private List<MypageResponse.PlanCardDTO> loadUpcomingTripPlans(Integer sessionUserId) {
        LocalDate today = LocalDate.now();
        LocalDate inclusiveToday = today.minusDays(1);

        // TripRepository 조회 조건이 startDate > 기준일이므로 오늘 출발 계획도 포함되게 하루를 보정한다.
        return tripRepository.findUpcomingPlanListByUserId(sessionUserId, inclusiveToday, 0, UPCOMING_TRIP_PLAN_LIMIT)
                .stream()
                .map(tripPlan -> MypageResponse.PlanCardDTO.from(tripPlan))
                .collect(Collectors.toList());
    }

    @Transactional
    public void changePassword(Integer sessionUserId, MypageRequest.ChangePasswordDTO reqDTO) {
        User user = findUser(sessionUserId);

        String currentPassword = normalize(reqDTO.getCurrentPassword());
        String newPassword = normalize(reqDTO.getNewPassword());
        String newPasswordConfirm = normalize(reqDTO.getNewPasswordConfirm());

        if (!normalize(user.getPassword()).equals(currentPassword)) {
            throw new Exception400("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new Exception400("새 비밀번호와 확인값이 일치하지 않습니다.");
        }

        user.changePassword(newPassword);
    }

    private MypageResponse.ProfileDTO loadProfile(Integer sessionUserId) {
        return MypageResponse.ProfileDTO.from(findUser(sessionUserId));
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
