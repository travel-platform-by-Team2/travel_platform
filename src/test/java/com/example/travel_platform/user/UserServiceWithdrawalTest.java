package com.example.travel_platform.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception400;
import com.example.travel_platform._core.handler.ex.Exception403;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class UserServiceWithdrawalTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager em;

    @Test
    void withdraw() {
        encodePassword(1, "1234");

        userService.withdrawAccount(1, "1234");

        assertEquals(0L, count("select count(u) from User u where u.id = :userId", 1));
        assertEquals(0L, count("select count(b) from Board b where b.user.id = :userId", 1));
        assertEquals(0L, count("select count(r) from Reply r where r.user.id = :userId", 1));
        assertEquals(0L, count("select count(r) from Reply r where r.board.user.id = :userId", 1));
        assertEquals(0L, count("select count(bl) from BoardLike bl where bl.user.id = :userId", 1));
        assertEquals(0L, count("select count(bl) from BoardLike bl where bl.board.user.id = :userId", 1));
        assertEquals(0L, count("select count(tp) from TripPlan tp where tp.user.id = :userId", 1));
        assertEquals(0L, count("select count(tp) from TripPlace tp where tp.tripPlan.user.id = :userId", 1));
        assertEquals(0L, count("select count(b) from Booking b where b.user.id = :userId", 1));
        assertEquals(0L, count("select count(b) from Booking b where b.tripPlan.user.id = :userId", 1));
        assertEquals(0L, count("select count(e) from CalendarEvent e where e.user.id = :userId", 1));
        assertEquals(0L, count("select count(e) from CalendarEvent e where e.tripPlan.user.id = :userId", 1));
    }

    @Test
    void wrongPw() {
        encodePassword(1, "1234");

        assertThrows(Exception400.class, () -> userService.withdrawAccount(1, "wrong-password"));
        assertEquals(1L, count("select count(u) from User u where u.id = :userId", 1));
    }

    @Test
    void admin() {
        encodePassword(3, "1234");

        assertThrows(Exception403.class, () -> userService.withdrawAccount(3, "1234"));
        assertEquals(1L, count("select count(u) from User u where u.id = :userId", 3));
    }

    private void encodePassword(Integer userId, String rawPassword) {
        User user = userQueryRepository.findUser(userId).orElseThrow();
        user.changePassword(passwordEncoder.encode(rawPassword));
        em.flush();
        em.clear();
    }

    private long count(String jpql, Integer userId) {
        return em.createQuery(jpql, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
