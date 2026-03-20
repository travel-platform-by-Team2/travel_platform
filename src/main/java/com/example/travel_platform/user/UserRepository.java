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
    public Optional<User> findById(Integer id) {
        User findUser = em.find(User.class, id);
        return Optional.ofNullable(findUser);
    }

    // 로그인할때 username으로 조회해서 password 검증
    public Optional<User> findByUsername(String username) {
        return em.createQuery("select u from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return em.createQuery("select u from User u where u.email = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<User> findByEmailAndProvider(String email, String provider) {
        return em.createQuery("select u from User u where u.email = :email and u.provider = :provider", User.class)
                .setParameter("email", email)
                .setParameter("provider", provider)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public void delete(User user) {
        User managedUser = em.contains(user) ? user : em.merge(user);
        em.remove(managedUser);

    }
}
