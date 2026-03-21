package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final EntityManager em;

    public Optional<User> findUser(Integer userId) {
        User user = em.find(User.class, userId);
        return Optional.ofNullable(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return em.createQuery("""
                select u
                from User u
                where u.username = :username
                """, User.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findUserByEmail(String email) {
        return em.createQuery("""
                select u
                from User u
                where u.email = :email
                """, User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findSnsUser(String email, String provider) {
        return em.createQuery("""
                select u
                from User u
                where u.email = :email
                  and u.provider = :provider
                """, User.class)
                .setParameter("email", email)
                .setParameter("provider", provider)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
