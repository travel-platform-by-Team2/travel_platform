package com.example.travel_platform.admin;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.travel_platform.user.User;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminUserQueryRepository {

    private final EntityManager em;

    public long countUsers() {
        return em.createQuery("select count(u) from User u", Long.class)
                .getSingleResult();
    }

    public long countInactiveUsers() {
        return em.createQuery("select count(u) from User u where u.active = false", Long.class)
                .getSingleResult();
    }

    public List<User> findAllUsersByCreatedAtDesc() {
        return em.createQuery("""
                select u
                from User u
                order by u.createdAt desc, u.id desc
                """, User.class)
                .getResultList();
    }

    public List<User> findUsersByActiveByCreatedAtDesc(boolean active) {
        return em.createQuery("""
                select u
                from User u
                where u.active = :active
                order by u.createdAt desc, u.id desc
                """, User.class)
                .setParameter("active", active)
                .getResultList();
    }

    public List<User> findUsersByKeyword(String keyword) {
        return em.createQuery("""
                select u
                from User u
                where u.username like concat('%', :keyword, '%')
                    or u.email like concat('%', :keyword, '%')
                order by u.createdAt desc, u.id desc
                """, User.class)
                .setParameter("keyword", keyword)
                .getResultList();
    }

    public List<User> findUsersByActiveAndKeyword(boolean active, String keyword) {
        return em.createQuery("""
                select u
                from User u
                where u.active = :active
                  and (
                      u.username like concat('%', :keyword, '%')
                      or u.email like concat('%', :keyword, '%')
                  )
                order by u.createdAt desc, u.id desc
                """, User.class)
                .setParameter("active", active)
                .setParameter("keyword", keyword)
                .getResultList();
    }

    public List<User> findRecentUsers(int limit) {
        return em.createQuery("""
                select u
                from User u
                order by u.createdAt desc, u.id desc
                """, User.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
