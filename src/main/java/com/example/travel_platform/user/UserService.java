package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception401;
import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform.board.BoardLikeRepository;
import com.example.travel_platform.board.BoardRepository;
import com.example.travel_platform.board.reply.ReplyRepository;
import com.example.travel_platform.booking.BookingRepository;
import com.example.travel_platform.calendar.CalendarRepository;
import com.example.travel_platform.trip.TripPlaceRepository;
import com.example.travel_platform.trip.TripRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserQueryRepository userQueryRepository;
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final ReplyRepository replyRepository;
    private final TripRepository tripRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final BookingRepository bookingRepository;
    private final CalendarRepository calendarRepository;

    @Transactional
    public void join(UserRequest.JoinDTO reqDTO) {
        validateJoinUsername(reqDTO.getUsername());
        userRepository.save(createUserFromJoinRequest(reqDTO));
    }

    public SessionUser login(UserRequest.LoginDTO reqDTO) {
        User user = findUserByLoginEmail(reqDTO.getEmail());
        validateLoginUser(user, reqDTO.getPassword());
        return createSessionUser(user);
    }

    @Transactional
    public SessionUser loginWithSns(String email, String username, String provider, String providerId) {
        UserAuthProvider providerType = resolveAuthProvider(provider);
        User user = findOrCreateSnsUser(email, username, providerType, providerId);
        ensureActiveSnsUser(user);
        return createSessionUser(user);
    }

    @Transactional
    public SessionUser snsLogin(String email, String username, String provider, String providerId) {
        return loginWithSns(email, username, provider, providerId);
    }

    public void withdrawAccount(Integer sessionUserId, String currentPassword) {
        User user = findUser(sessionUserId);
        validateWithdraw(user, currentPassword);
        deleteRelatedBoardData(sessionUserId);
        deleteRelatedTripData(sessionUserId);
        userRepository.delete(user);
    }

    private void validateJoinUsername(String username) {
        Optional<User> user = userQueryRepository.findUserByUsername(username);
        if (user.isPresent()) {
            throw new Exception400("유저 네임이 중복되었습니다!!");
        }
    }

    private User createUserFromJoinRequest(UserRequest.JoinDTO reqDTO) {
        return User.create(
                reqDTO.getUsername(),
                reqDTO.getPassword(),
                reqDTO.getEmail(),
                reqDTO.getTel(),
                "USER");
    }

    private User findUserByLoginEmail(String email) {
        return userQueryRepository.findUserByEmail(email)
                .orElseThrow(() -> new Exception400("email을 찾을 수가 없어요"));
    }

    private void validateLoginUser(User user, String password) {
        if (!user.isAdmin() && !user.isActive()) {
            throw new Exception403("현재 로그인할 수 없는 계정입니다");
        }

        if (!normalize(user.getPassword()).equals(normalize(password))) {
            throw new Exception401("패스워드가 일치하지 않아요");
        }
    }

    private User findOrCreateSnsUser(String email, String username, UserAuthProvider provider, String providerId) {
        return userQueryRepository.findSnsUser(email, provider)
                .orElseGet(() -> userRepository.save(createSnsUser(email, username, provider, providerId)));
    }

    private User createSnsUser(String email, String username, UserAuthProvider provider, String providerId) {
        String safeBaseName = (username == null || username.isBlank()) ? provider.getCode() : username;
        String suffix = providerId.length() > 4 ? providerId.substring(providerId.length() - 4) : providerId;
        User user = User.createSNS(safeBaseName + "_" + suffix, email, provider.getCode(), providerId);
        user.setActive(true);
        return user;
    }

    private void ensureActiveSnsUser(User user) {
        if (!user.isActive()) {
            user.setActive(true);
        }
    }

    private SessionUser createSessionUser(User user) {
        return SessionUser.fromUserEntity(user);
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자 정보를 찾을 수 없습니다."));
    }

    private void validateWithdraw(User user, String currentPassword) {
        if (user.isAdmin()) {
            throw new Exception403("관리자 계정은 탈퇴할 수 없습니다.");
        }

        if (!normalize(user.getPassword()).equals(normalize(currentPassword))) {
            throw new Exception400("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private void deleteRelatedBoardData(Integer userId) {
        boardLikeRepository.deleteByUserId(userId);
        replyRepository.deleteByUserId(userId);

        boardLikeRepository.deleteByBoardUserId(userId);
        replyRepository.deleteByBoardUserId(userId);
        boardRepository.deleteByUserId(userId);
    }

    private void deleteRelatedTripData(Integer userId) {
        calendarRepository.deleteByUserId(userId);
        calendarRepository.deleteByTripPlanUserId(userId);

        bookingRepository.deleteByUserId(userId);
        bookingRepository.deleteByTripPlanUserId(userId);

        tripPlaceRepository.deleteByTripPlanUserId(userId);
        tripRepository.deleteByUserId(userId);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private UserAuthProvider resolveAuthProvider(String providerCode) {
        try {
            return UserAuthProvider.fromCode(providerCode);
        } catch (IllegalArgumentException e) {
            throw new Exception400("지원하지 않는 SNS 제공자입니다.");
        }
    }
}
