package com.example.travel_platform.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardRepository {

    private final EntityManager em;

    public Board save(Board board) {
        if (board.getId() == null) {
            em.persist(board);
            return board;
        }
        return em.merge(board);
    }

    public Optional<Board> findById(Integer boardId) {
        return Optional.ofNullable(em.find(Board.class, boardId));
    }

    public List<Board> findAll() {
        return em.createQuery("""
                select b
                from Board b
                order by b.id desc
                """, Board.class).getResultList();
    }

    public List<Board> findAllPaging(String sort, int offset, int size) {
        String jpql = """
                select b
                from Board b
                order by
                """ + " " + toOrderBy(sort);

        return em.createQuery(jpql, Board.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Board> findAllPaging(int offset, int size) {
        return findAllPaging("latest", offset, size);
    }

    public long count() {
        return em.createQuery("""
                select count(b)
                from Board b
                """, Long.class)
                .getSingleResult();
    }

    public List<Board> findAllPagingByCategory(String category, String sort, int offset, int size) {
        String jpql = """
                select b
                from Board b
                where b.category = :category
                order by
                """ + " " + toOrderBy(sort);

        return em.createQuery(jpql, Board.class)
                .setParameter("category", category)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public List<Board> findAllPagingByCategory(String category, int offset, int size) {
        return findAllPagingByCategory(category, "latest", offset, size);
    }

    public long countByCategory(String category) {
        return em.createQuery("""
                select count(b)
                from Board b
                where b.category = :category
                """, Long.class)
                .setParameter("category", category)
                .getSingleResult();
    }

    public void delete(Board board) {
        em.remove(board);
    }

    @Transactional
    public int deleteByUserId(Integer userId) {
        return em.createQuery("""
                delete from Board b
                where b.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public Map<Integer, Long> countLikesByBoardIds(List<Integer> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery("""
                select bl.board.id, count(bl)
                from BoardLike bl
                where bl.board.id in :boardIds
                group by bl.board.id
                """, Object[].class)
                .setParameter("boardIds", boardIds)
                .getResultList();

        Map<Integer, Long> likeCounts = new HashMap<>();
        for (Object[] row : rows) {
            Integer boardId = (Integer) row[0];
            Long likeCount = (Long) row[1];
            likeCounts.put(boardId, likeCount);
        }
        return likeCounts;
    }

    @Transactional
    public int deleteLikesByBoard(Integer boardId) {
        return em.createNativeQuery("""
                delete from board_like_tb
                where board_id = :boardId
                """)
                .setParameter("boardId", boardId)
                .executeUpdate();
    }

    public List<Board> search(String category, String[] words, int offset, int size) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("select b from Board b where 1=1 ");

        boolean hasCategory = category != null && !category.isBlank();
        if (hasCategory) {
            jpql.append("and b.category = :category ");
        }

        for (int i = 0; i < words.length; i++) {
            jpql.append("and (");
            jpql.append("lower(b.title) like :titleWord").append(i).append(" ");
            jpql.append("or b.content like :contentWord").append(i).append(" ");
            jpql.append(") ");
        }

        jpql.append("order by b.id desc");

        TypedQuery<Board> query = em.createQuery(jpql.toString(), Board.class);

        if (hasCategory) {
            query.setParameter("category", category);
        }

        for (int i = 0; i < words.length; i++) {
            query.setParameter("titleWord" + i, "%" + words[i].toLowerCase() + "%");
            query.setParameter("contentWord" + i, "%" + words[i] + "%");
        }

        return query.setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countSearch(String category, String[] words) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("select count(b) from Board b where 1=1 ");

        boolean hasCategory = category != null && !category.isBlank();
        if (hasCategory) {
            jpql.append("and b.category = :category ");
        }

        for (int i = 0; i < words.length; i++) {
            jpql.append("and (");
            jpql.append("lower(b.title) like :titleWord").append(i).append(" ");
            jpql.append("or b.content like :contentWord").append(i).append(" ");
            jpql.append(") ");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (hasCategory) {
            query.setParameter("category", category);
        }

        for (int i = 0; i < words.length; i++) {
            query.setParameter("titleWord" + i, "%" + words[i].toLowerCase() + "%");
            query.setParameter("contentWord" + i, "%" + words[i] + "%");
        }

        return query.getSingleResult();
    }

    private String toOrderBy(String sort) {
        return switch (sort) {
            case "likes" -> "(select count(bl) from BoardLike bl where bl.board = b) desc, b.createdAt desc, b.id desc";
            case "views" -> "b.viewCount desc, b.createdAt desc, b.id desc";
            case "date" -> "b.createdAt asc, b.id asc";
            default -> "b.createdAt desc, b.id desc";
        };
    }
}
