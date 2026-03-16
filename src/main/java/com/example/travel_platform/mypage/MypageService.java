package com.example.travel_platform.mypage;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MypageService {

    private final UserRepository userRepository;

    public MypageResponse.MainDTO getMainPage(Integer sessionUserId) {
        MypageResponse.ProfileDTO user = loadProfile(sessionUserId);
        List<MypageResponse.BookingCardDTO> bookings = List.of();
        List<MypageResponse.PlanCardDTO> tripPlans = List.of();

        return MypageResponse.MainDTO.builder()
                .user(user)
                .hasBookings(!bookings.isEmpty())
                .bookings(bookings)
                .hasTripPlans(!tripPlans.isEmpty())
                .tripPlans(tripPlans)
                .build();
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

        user.setPassword(newPassword);
    }

    private MypageResponse.ProfileDTO loadProfile(Integer sessionUserId) {
        User user = findUser(sessionUserId);

        return MypageResponse.ProfileDTO.builder()
                .id(user.getId())
                .username(normalize(user.getUsername()))
                .email(normalize(user.getEmail()))
                .build();
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
