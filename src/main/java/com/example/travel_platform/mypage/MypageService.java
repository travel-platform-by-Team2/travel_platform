package com.example.travel_platform.mypage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripRepository;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MypageService {

    private static final int UPCOMING_TRIP_PLAN_LIMIT = 2;
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    public MypageResponse.MainDTO getMainPage(Integer sessionUserId) {
        MypageResponse.ProfileDTO user = loadProfile(sessionUserId);
        List<MypageResponse.BookingCardDTO> bookings = List.of();
        List<MypageResponse.PlanCardDTO> tripPlans = loadUpcomingTripPlans(sessionUserId);

        return MypageResponse.MainDTO.builder()
                .user(user)
                .hasBookings(!bookings.isEmpty())
                .bookings(bookings)
                .hasTripPlans(!tripPlans.isEmpty())
                .tripPlans(tripPlans)
                .build();
    }

    private List<MypageResponse.PlanCardDTO> loadUpcomingTripPlans(Integer sessionUserId) {
        LocalDate today = LocalDate.now();
        LocalDate inclusiveToday = today.minusDays(1);

        // TripRepository의 예정 여행 조회는 `startDate > 기준일` 조건이므로, 오늘 포함 요구사항은 기준일을 하루 앞당겨 맞춘다.
        return tripRepository.findUpcomingPlanListByUserId(sessionUserId, inclusiveToday, 0, UPCOMING_TRIP_PLAN_LIMIT)
                .stream()
                .map(this::toPlanCardDTO)
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
        User user = findUser(sessionUserId);

        return MypageResponse.ProfileDTO.builder()
                .id(user.getId())
                .username(normalize(user.getUsername()))
                .email(normalize(user.getEmail()))
                .build();
    }

    private MypageResponse.PlanCardDTO toPlanCardDTO(TripPlan tripPlan) {
        return MypageResponse.PlanCardDTO.builder()
                .id(tripPlan.getId())
                .title(normalize(tripPlan.getTitle()))
                .dateRangeLabel(formatDateRange(tripPlan.getStartDate(), tripPlan.getEndDate()))
                .build();
    }

    private User findUser(Integer sessionUserId) {
        return userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception400("사용자 정보를 찾을 수 없습니다."));
    }

    private String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return startDate.format(DATE_LABEL_FORMATTER) + " - " + endDate.format(DATE_LABEL_FORMATTER);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
