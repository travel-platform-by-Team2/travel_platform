package com.example.travel_platform.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public User save(User user) {
        em.persist(user);
        return user;
    }

    public Optional<User> findById(Integer userId) {
        User user = em.find(User.class, userId);
        return Optional.ofNullable(user);
    }

    public void delete(User user) {
        User managedUser = em.contains(user) ? user : em.merge(user);
        em.remove(managedUser);
    }
}
