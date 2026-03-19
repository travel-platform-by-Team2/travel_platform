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
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final ReplyRepository replyRepository;
    private final TripRepository tripRepository;
    private final TripPlaceRepository tripPlaceRepository;
    private final BookingRepository bookingRepository;
    private final CalendarRepository calendarRepository;

    // 회원가입 실패 : 중복 체크 후 예외발생 :지윤
    @Transactional
    public void join(UserRequest.JoinDTO reqDTO) {
        // 1. 유저네임 중복 체크 (필터링)!!
        Optional<User> optUser = userRepository.findByUsername(reqDTO.getUsername());

        if (optUser.isPresent()) {
            throw new Exception400("유저 네임이 중복되었습니다!!!");
        }

        User user = User.create(
                reqDTO.getUsername(),
                reqDTO.getPassword(),
                reqDTO.getEmail(),
                reqDTO.getTel(),
                "USER");
        userRepository.save(user);
    }

    // 로그인 실패: 이메일/비밀번호 확인 후 예외 발생 (지윤)
    public SessionUser login(UserRequest.LoginDTO reqDTO) {

        User findUser = userRepository.findByEmail(reqDTO.getEmail())
                .orElseThrow(() -> new Exception400("email을 찾을 수가 없어요"));

        if (!findUser.getPassword().equals(reqDTO.getPassword())) {
            throw new Exception401("패스워드가 일치하지 않아요");
        }

        return SessionUser.from(findUser);
    }

    @Transactional
    public void withdrawAccount(Integer sessionUserId, String currentPassword) {
        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception400("사용자 정보를 찾을 수 없습니다."));

        if (user.isAdmin()) {
            throw new Exception403("관리자 계정은 탈퇴할 수 없습니다.");
        }

        if (!normalize(user.getPassword()).equals(normalize(currentPassword))) {
            throw new Exception400("현재 비밀번호가 일치하지 않습니다.");
        }

        deleteBoardData(sessionUserId);
        deleteTripData(sessionUserId);
        userRepository.delete(user);
    }

    private void deleteBoardData(Integer userId) {
        boardLikeRepository.deleteByUserId(userId);
        replyRepository.deleteByUserId(userId);

        boardLikeRepository.deleteByBoardUserId(userId);
        replyRepository.deleteByBoardUserId(userId);
        boardRepository.deleteByUserId(userId);
    }

    private void deleteTripData(Integer userId) {
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
}
