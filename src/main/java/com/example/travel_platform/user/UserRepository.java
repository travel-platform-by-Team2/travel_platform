package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    // 회원가입할 떄 insert
    public User save(User user) {
        em.persist(user);
        return user;
    }

    //
    public Optional<User> findById(int id) {
        User findUser = em.find(User.class, id);
        return Optional.ofNullable(findUser);
    }

    // 로그인할때 username으로 조회해서 password 검증
    public Optional<User> findByUsername(String username) {
        try {
            User user = em.createQuery("select u from User u where u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.ofNullable(null);
        }
    }
}
