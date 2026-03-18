package com.example.travel_platform.board;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

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

    // 페이징 조회
    public List<Board> findAllPaging(int offset, int size) {
        return em.createQuery("""
                select b
                from Board b
                order by b.id desc
                """, Board.class)
                .setFirstResult(offset) // 몇 개 건너뛸지
                .setMaxResults(size) // 몇 개 가져올지
                .getResultList();
    }

    // 전체 개수 조회
    public long count() {
        return em.createQuery("""
                select count(b)
                from Board b
                """, Long.class)
                .getSingleResult();
    }

    // 카테고리
    public List<Board> findAllPagingByCategory(String category, int offset, int size) {
        return em.createQuery("""
                select b
                from Board b
                where b.category = :category
                order by b.id desc
                """, Board.class)
                .setParameter("category", category)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    // 카테고리 전체 페이지
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
}
