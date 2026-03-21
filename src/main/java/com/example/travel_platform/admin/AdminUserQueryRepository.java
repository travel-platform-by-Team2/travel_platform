package com.example.travel_platform.admin;

import java.util.List;

import org.springframework.stereotype.Repository;

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

    public List<AdminUserSummaryRow> findAllUserSummaryRowsByCreatedAtDesc() {
        return em.createQuery("""
                select new com.example.travel_platform.admin.AdminUserSummaryRow(
                    u.id,
                    u.username,
                    u.email,
                    u.createdAt,
                    u.active,
                    count(b)
                )
                from User u
                left join Board b on b.user = u
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, AdminUserSummaryRow.class)
                .getResultList();
    }

    public List<AdminUserSummaryRow> findUserSummaryRowsByActiveByCreatedAtDesc(boolean active) {
        return em.createQuery("""
                select new com.example.travel_platform.admin.AdminUserSummaryRow(
                    u.id,
                    u.username,
                    u.email,
                    u.createdAt,
                    u.active,
                    count(b)
                )
                from User u
                left join Board b on b.user = u
                where u.active = :active
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, AdminUserSummaryRow.class)
                .setParameter("active", active)
                .getResultList();
    }

    public List<AdminUserSummaryRow> findUserSummaryRowsByKeyword(String keyword) {
        return em.createQuery("""
                select new com.example.travel_platform.admin.AdminUserSummaryRow(
                    u.id,
                    u.username,
                    u.email,
                    u.createdAt,
                    u.active,
                    count(b)
                )
                from User u
                left join Board b on b.user = u
                where u.username like concat('%', :keyword, '%')
                    or u.email like concat('%', :keyword, '%')
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, AdminUserSummaryRow.class)
                .setParameter("keyword", keyword)
                .getResultList();
    }

    public List<AdminUserSummaryRow> findUserSummaryRowsByActiveAndKeyword(boolean active, String keyword) {
        return em.createQuery("""
                select new com.example.travel_platform.admin.AdminUserSummaryRow(
                    u.id,
                    u.username,
                    u.email,
                    u.createdAt,
                    u.active,
                    count(b)
                )
                from User u
                left join Board b on b.user = u
                where u.active = :active
                  and (
                      u.username like concat('%', :keyword, '%')
                      or u.email like concat('%', :keyword, '%')
                  )
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, AdminUserSummaryRow.class)
                .setParameter("active", active)
                .setParameter("keyword", keyword)
                .getResultList();
    }

    public List<AdminRecentUserRow> findRecentUserRows(int limit) {
        return em.createQuery("""
                select new com.example.travel_platform.admin.AdminRecentUserRow(
                    u.username,
                    u.active,
                    u.createdAt
                )
                from User u
                order by u.createdAt desc, u.id desc
                """, AdminRecentUserRow.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
