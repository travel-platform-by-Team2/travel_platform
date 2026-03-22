package com.example.travel_platform.board;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardQueryRepository {

    private final EntityManager em;

    public long count() {
        return em.createQuery("""
                select count(b)
                from Board b
                """, Long.class)
                .getSingleResult();
    }

    public long countByCreatedAtAfter(LocalDateTime since) {
        return em.createQuery("""
                select count(b)
                from Board b
                where b.createdAt >= :since
                """, Long.class)
                .setParameter("since", since)
                .getSingleResult();
    }

    public long sumViewCount() {
        Long totalViewCount = em.createQuery("""
                select coalesce(sum(b.viewCount), 0)
                from Board b
                """, Long.class)
                .getSingleResult();
        return totalViewCount == null ? 0L : totalViewCount;
    }

    public List<Board> findAllPaging(String sort, int offset, int size) {
        String jpql = """
                select b
                from Board b
                join fetch b.user
                order by
                """ + " " + toOrderBy(sort);

        return em.createQuery(jpql, Board.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Board> findAllPagingByCategory(BoardCategory category, String sort, int offset, int size) {
        String jpql = """
                select b
                from Board b
                join fetch b.user
                where b.category = :category
                order by
                """ + " " + toOrderBy(sort);

        return em.createQuery(jpql, Board.class)
                .setParameter("category", category)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Board> findRecentBoards(int size) {
        return em.createQuery("""
                select b
                from Board b
                join fetch b.user
                order by b.createdAt desc, b.id desc
                """, Board.class)
                .setMaxResults(size)
                .getResultList();
    }

    public long countByCategory(BoardCategory category) {
        return em.createQuery("""
                select count(b)
                from Board b
                where b.category = :category
                """, Long.class)
                .setParameter("category", category)
                .getSingleResult();
    }

    public Map<Integer, Long> countBoardsByUserIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery("""
                select b.user.id, count(b)
                from Board b
                where b.user.id in :userIds
                group by b.user.id
                """, Object[].class)
                .setParameter("userIds", userIds)
                .getResultList();

        Map<Integer, Long> boardCounts = new HashMap<>();
        for (Object[] row : rows) {
            boardCounts.put((Integer) row[0], (Long) row[1]);
        }
        return boardCounts;
    }

    public List<Board> search(BoardCategory category, String[] words, String sort, int offset, int size) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("select b from Board b join fetch b.user where 1=1 ");

        boolean hasCategory = category != null;
        if (hasCategory) {
            jpql.append("and b.category = :category ");
        }

        for (int i = 0; i < words.length; i++) {
            jpql.append("and (");
            jpql.append("lower(b.title) like :titleWord").append(i).append(" ");
            jpql.append("or lower(cast(b.content as string)) like :contentWord").append(i).append(" ");
            jpql.append(") ");
        }

        jpql.append("order by ").append(toOrderBy(sort));

        TypedQuery<Board> query = em.createQuery(jpql.toString(), Board.class);

        if (hasCategory) {
            query.setParameter("category", category);
        }

        for (int i = 0; i < words.length; i++) {
            query.setParameter("titleWord" + i, "%" + words[i].toLowerCase() + "%");
            query.setParameter("contentWord" + i, "%" + words[i].toLowerCase() + "%");
        }

        return query.setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countSearch(BoardCategory category, String[] words) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("select count(b) from Board b where 1=1 ");

        boolean hasCategory = category != null;
        if (hasCategory) {
            jpql.append("and b.category = :category ");
        }

        for (int i = 0; i < words.length; i++) {
            jpql.append("and (");
            jpql.append("lower(b.title) like :titleWord").append(i).append(" ");
            jpql.append("or lower(cast(b.content as string)) like :contentWord").append(i).append(" ");
            jpql.append(") ");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (hasCategory) {
            query.setParameter("category", category);
        }

        for (int i = 0; i < words.length; i++) {
            query.setParameter("titleWord" + i, "%" + words[i].toLowerCase() + "%");
            query.setParameter("contentWord" + i, "%" + words[i].toLowerCase() + "%");
        }

        return query.getSingleResult();
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
