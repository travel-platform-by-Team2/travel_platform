package com.example.travel_platform.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.travel_platform.board.BoardCategory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminQueryRepository {

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
        List<Tuple> tuples = em.createQuery("""
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, Tuple.class)
                .getResultList();
        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findUserSummaryRowsByActiveByCreatedAtDesc(boolean active) {
        List<Tuple> tuples = em.createQuery("""
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                where u.active = :active
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, Tuple.class)
                .setParameter("active", active)
                .getResultList();
        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findUserSummaryRowsByKeyword(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase();
        List<Tuple> tuples = em.createQuery("""
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                where lower(u.username) like concat('%', :keyword, '%')
                    or lower(u.email) like concat('%', :keyword, '%')
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, Tuple.class)
                .setParameter("keyword", normalizedKeyword)
                .getResultList();
        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findUserSummaryRowsByActiveAndKeyword(boolean active, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase();
        List<Tuple> tuples = em.createQuery("""
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                where u.active = :active
                  and (
                      lower(u.username) like concat('%', :keyword, '%')
                      or lower(u.email) like concat('%', :keyword, '%')
                  )
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by u.createdAt desc, u.id desc
                """, Tuple.class)
                .setParameter("active", active)
                .setParameter("keyword", normalizedKeyword)
                .getResultList();
        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findPagedUsers(String sortBy, String orderBy, int offset, int limit) {
        // 1. 사용자 목록에 필요한 컬럼과 게시글 수 집계값을 함께 조회한다.
        String jpql = """
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by
                """ + toUserOrderBy(sortBy, orderBy);

        // 2. 정렬이 끝난 전체 결과에서 현재 페이지에 필요한 구간만 잘라온다.
        List<Tuple> tuples = em.createQuery(jpql, Tuple.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        // 3. Tuple 결과를 서비스가 쓰기 쉬운 SummaryRow로 변환한다.
        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findPagedUsersByActive(
            boolean active,
            String sortBy,
            String orderBy,
            int offset,
            int limit) {
        String jpql = """
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                where u.active = :active
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by
                """ + toUserOrderBy(sortBy, orderBy);

        List<Tuple> tuples = em.createQuery(jpql, Tuple.class)
                .setParameter("active", active)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findPagedUsersByKeyword(
            String keyword,
            String sortBy,
            String orderBy,
            int offset,
            int limit) {
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase();
        String jpql = """
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                where lower(u.username) like concat('%', :keyword, '%')
                   or lower(u.email) like concat('%', :keyword, '%')
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by
                """ + toUserOrderBy(sortBy, orderBy);

        List<Tuple> tuples = em.createQuery(jpql, Tuple.class)
                .setParameter("keyword", normalizedKeyword)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminUserSummaryRow> findPagedUsersByActiveAndKeyword(
            boolean active,
            String keyword,
            String sortBy,
            String orderBy,
            int offset,
            int limit) {
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase();
        String jpql = """
                select
                    u.id as userId,
                    u.username as username,
                    u.email as email,
                    u.createdAt as createdAt,
                    u.active as active,
                    count(b) as boardCount
                from User u
                left join Board b on b.user = u
                where u.active = :active
                  and (
                      lower(u.username) like concat('%', :keyword, '%')
                      or lower(u.email) like concat('%', :keyword, '%')
                  )
                group by u.id, u.username, u.email, u.createdAt, u.active
                order by
                """ + toUserOrderBy(sortBy, orderBy);

        List<Tuple> tuples = em.createQuery(jpql, Tuple.class)
                .setParameter("active", active)
                .setParameter("keyword", normalizedKeyword)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return toAdminUserSummaryRows(tuples);
    }

    public List<AdminRecentUserRow> findRecentUserRows(int limit) {
        List<Tuple> tuples = em.createQuery("""
                select
                    u.username as username,
                    u.active as active,
                    u.createdAt as createdAt
                from User u
                order by u.createdAt desc, u.id desc
                """, Tuple.class)
                .setMaxResults(limit)
                .getResultList();
        return toAdminRecentUserRows(tuples);
    }

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
        List<Tuple> tuples = em.createQuery("""
                select
                    b.id as boardId,
                    b.title as title,
                    u.username as userName,
                    b.createdAt as createdAt
                from Board b
                join b.user u
                order by b.createdAt desc, b.id desc
                """, Tuple.class)
                .setMaxResults(size)
                .getResultList();
        return toAdminRecentBoardRows(tuples);
    }

    public List<AdminBoardSummaryRow> findBoardSummaryRows(
            BoardCategory category,
            String[] words,
            String sort,
            int offset,
            int size) {
        StringBuilder jpql = new StringBuilder("""
                select
                    b.id as boardId,
                    b.title as title,
                    u.username as userName,
                    b.createdAt as createdAt,
                    b.viewCount as viewCount,
                    b.category as category
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
                jpql.append(" lower(b.title) like :word").append(i);
                jpql.append(" or lower(cast(b.content as string)) like :word").append(i);
                jpql.append(" )");
            }
        }

        jpql.append(" order by ").append(toOrderBy(sort));

        TypedQuery<Tuple> query = em.createQuery(jpql.toString(), Tuple.class);
        bindSearchParameters(query, category, words, hasCategory, hasKeyword);

        List<Tuple> tuples = query.setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
        return toAdminBoardSummaryRows(tuples);
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
                jpql.append(" lower(b.title) like :word").append(i);
                jpql.append(" or lower(cast(b.content as string)) like :word").append(i);
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
                query.setParameter("word" + i, "%" + words[i].toLowerCase() + "%");
            }
        }
    }

    private AdminUserSummaryRow toAdminUserSummaryRow(Tuple tuple) {
        return new AdminUserSummaryRow(
                tuple.get("userId", Integer.class),
                tuple.get("username", String.class),
                tuple.get("email", String.class),
                tuple.get("createdAt", LocalDateTime.class),
                tuple.get("active", Boolean.class),
                tuple.get("boardCount", Long.class));
    }

    private List<AdminUserSummaryRow> toAdminUserSummaryRows(List<Tuple> tuples) {
        List<AdminUserSummaryRow> rows = new java.util.ArrayList<>();
        for (Tuple tuple : tuples) {
            rows.add(toAdminUserSummaryRow(tuple));
        }
        return rows;
    }

    private AdminRecentUserRow toAdminRecentUserRow(Tuple tuple) {
        return new AdminRecentUserRow(
                tuple.get("username", String.class),
                tuple.get("active", Boolean.class),
                tuple.get("createdAt", LocalDateTime.class));
    }

    private List<AdminRecentUserRow> toAdminRecentUserRows(List<Tuple> tuples) {
        List<AdminRecentUserRow> rows = new java.util.ArrayList<>();
        for (Tuple tuple : tuples) {
            rows.add(toAdminRecentUserRow(tuple));
        }
        return rows;
    }

    private AdminBoardSummaryRow toAdminBoardSummaryRow(Tuple tuple) {
        return new AdminBoardSummaryRow(
                tuple.get("boardId", Integer.class),
                tuple.get("title", String.class),
                tuple.get("userName", String.class),
                tuple.get("createdAt", LocalDateTime.class),
                tuple.get("viewCount", Integer.class),
                tuple.get("category", BoardCategory.class));
    }

    private AdminRecentBoardRow toAdminRecentBoardRow(Tuple tuple) {
        return new AdminRecentBoardRow(
                tuple.get("boardId", Integer.class),
                tuple.get("title", String.class),
                tuple.get("userName", String.class),
                tuple.get("createdAt", LocalDateTime.class));
    }

    private List<AdminBoardSummaryRow> toAdminBoardSummaryRows(List<Tuple> tuples) {
        List<AdminBoardSummaryRow> rows = new java.util.ArrayList<>();
        for (Tuple tuple : tuples) {
            rows.add(toAdminBoardSummaryRow(tuple));
        }
        return rows;
    }

    private List<AdminRecentBoardRow> toAdminRecentBoardRows(List<Tuple> tuples) {
        List<AdminRecentBoardRow> rows = new java.util.ArrayList<>();
        for (Tuple tuple : tuples) {
            rows.add(toAdminRecentBoardRow(tuple));
        }
        return rows;
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

    public long countAllUsers() {
        return em.createQuery("select count(u) from User u", Long.class).getSingleResult();
    }

    public long countUsersByActive(boolean active) {
        return em.createQuery("select count(u) from User u where u.active = :active", Long.class)
                .setParameter("active", active)
                .getSingleResult();
    }

    public long countUsersByKeyword(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase();

        return em.createQuery("""
                select count(u)
                from User u
                where lower(u.username) like concat('%', :keyword, '%')
                   or lower(u.email) like concat('%', :keyword, '%')
                """, Long.class)
                .setParameter("keyword", normalizedKeyword)
                .getSingleResult();
    }

    public long countUsersByActiveAndKeyword(boolean active, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.toLowerCase();

        return em.createQuery("""
                select count(u)
                from User u
                where u.active = :active
                  and (
                      lower(u.username) like concat('%', :keyword, '%')
                      or lower(u.email) like concat('%', :keyword, '%')
                  )
                """, Long.class)
                .setParameter("active", active)
                .setParameter("keyword", normalizedKeyword)
                .getSingleResult();
    }

    private String toUserOrderBy(String sortBy, String orderBy) {
        // 사용자 목록 페이징에서는 전체 결과를 먼저 정렬한 뒤 현재 페이지 분량만 잘라야 한다.
        if ("postCount".equals(sortBy)) {
            // 게시글 수 기준 오름차순: 게시글 수가 적은 사용자부터 보여준다.
            if ("asc".equals(orderBy)) {
                return "count(b) asc, u.id asc";
            }
            // 게시글 수 기준 내림차순: 게시글 수가 많은 사용자부터 보여준다.
            return "count(b) desc, u.id asc";
        }

        // 가입일 기준 오름차순: 오래전에 가입한 사용자부터 보여준다.
        if ("asc".equals(orderBy)) {
            return "u.createdAt asc, u.id asc";
        }
        // 가입일 기준 내림차순: 최근에 가입한 사용자부터 보여준다.
        return "u.createdAt desc, u.id asc";
    }

}
