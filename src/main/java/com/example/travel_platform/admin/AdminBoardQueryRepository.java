package com.example.travel_platform.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.travel_platform.board.BoardCategory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminBoardQueryRepository {

    private final EntityManager em;

    public long countBoards() {
        return em.createQuery("""
                select count(b)
                from Board b
                """, Long.class)
                .getSingleResult();
    }

    public long countBoardsByCreatedAtAfter(LocalDateTime since) {
        return em.createQuery("""
                select count(b)
                from Board b
                where b.createdAt >= :since
                """, Long.class)
                .setParameter("since", since)
                .getSingleResult();
    }

    public long sumBoardViewCount() {
        Long totalViewCount = em.createQuery("""
                select coalesce(sum(b.viewCount), 0)
                from Board b
                """, Long.class)
                .getSingleResult();
        return totalViewCount == null ? 0L : totalViewCount;
    }

    public long countBoardsByCategory(BoardCategory category) {
        return em.createQuery("""
                select count(b)
                from Board b
                where b.category = :category
                """, Long.class)
                .setParameter("category", category)
                .getSingleResult();
    }

    public List<AdminRecentBoardRow> findRecentBoardRows(int size) {
        return em.createQuery("""
                select new com.example.travel_platform.admin.AdminRecentBoardRow(
                    b.id,
                    b.title,
                    u.username,
                    b.createdAt
                )
                from Board b
                join b.user u
                order by b.createdAt desc, b.id desc
                """, AdminRecentBoardRow.class)
                .setMaxResults(size)
                .getResultList();
    }

    public List<AdminBoardSummaryRow> findBoardSummaryRows(
            BoardCategory category,
            String[] words,
            String sort,
            int offset,
            int size) {
        StringBuilder jpql = new StringBuilder("""
                select new com.example.travel_platform.admin.AdminBoardSummaryRow(
                    b.id,
                    b.title,
                    u.username,
                    b.createdAt,
                    b.viewCount,
                    b.category
                )
                from Board b
                join b.user u
                where 1=1
                """);

        boolean hasCategory = category != null;
        boolean hasKeyword = words != null && words.length > 0;

        if (hasCategory) {
            jpql.append(" and b.category = :category");
        }

        if (hasKeyword) {
            for (int i = 0; i < words.length; i++) {
                jpql.append(" and (");
                jpql.append(" lower(b.title) like :titleWord").append(i);
                jpql.append(" or b.content like :contentWord").append(i);
                jpql.append(" )");
            }
        }

        jpql.append(" order by ").append(toOrderBy(sort));

        TypedQuery<AdminBoardSummaryRow> query = em.createQuery(jpql.toString(), AdminBoardSummaryRow.class);

        bindSearchParameters(query, category, words, hasCategory, hasKeyword);

        return query.setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countBoardSummaryRows(BoardCategory category, String[] words) {
        StringBuilder jpql = new StringBuilder("""
                select count(b)
                from Board b
                where 1=1
                """);

        boolean hasCategory = category != null;
        boolean hasKeyword = words != null && words.length > 0;

        if (hasCategory) {
            jpql.append(" and b.category = :category");
        }

        if (hasKeyword) {
            for (int i = 0; i < words.length; i++) {
                jpql.append(" and (");
                jpql.append(" lower(b.title) like :titleWord").append(i);
                jpql.append(" or b.content like :contentWord").append(i);
                jpql.append(" )");
            }
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        bindSearchParameters(query, category, words, hasCategory, hasKeyword);

        return query.getSingleResult();
    }

    private void bindSearchParameters(
            TypedQuery<?> query,
            BoardCategory category,
            String[] words,
            boolean hasCategory,
            boolean hasKeyword) {
        if (hasCategory) {
            query.setParameter("category", category);
        }

        if (hasKeyword) {
            for (int i = 0; i < words.length; i++) {
                query.setParameter("titleWord" + i, "%" + words[i].toLowerCase() + "%");
                query.setParameter("contentWord" + i, "%" + words[i] + "%");
            }
        }
    }

    private String toOrderBy(String sort) {
        return switch (sort) {
            case "likes" -> "(select count(bl) from BoardLike bl where bl.board = b) desc, b.createdAt desc, b.id desc";
            case "downlikes" ->
                "(select count(bl) from BoardLike bl where bl.board = b) asc, b.createdAt asc, b.id asc";
            case "view" -> "b.viewCount desc, b.createdAt desc, b.id desc";
            case "downview" -> "b.viewCount asc, b.createdAt asc, b.id asc";
            case "date" -> "b.createdAt asc, b.id asc";
            default -> "b.createdAt desc, b.id desc";
        };
    }
}
