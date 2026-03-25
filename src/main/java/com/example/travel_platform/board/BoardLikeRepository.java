package com.example.travel_platform.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardLikeRepository {

    private final EntityManager em;

    public BoardLike save(BoardLike boardLike) {
        if (boardLike.getId() == null) {
            em.persist(boardLike);
            return boardLike;
        }
        return em.merge(boardLike);
    }

    public void delete(BoardLike boardLike) {
        BoardLike managedBoardLike = em.contains(boardLike) ? boardLike : em.merge(boardLike);
        em.remove(managedBoardLike);
    }

    public Optional<BoardLike> findByBoard_IdAndUser_Id(Integer boardId, Integer userId) {
        return em.createQuery("""
                select bl
                from BoardLike bl
                where bl.board.id = :boardId
                  and bl.user.id = :userId
                """, BoardLike.class)
                .setParameter("boardId", boardId)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByBoard_IdAndUser_Id(Integer boardId, Integer userId) {
        Long count = em.createQuery("""
                select count(bl)
                from BoardLike bl
                where bl.board.id = :boardId
                  and bl.user.id = :userId
                """, Long.class)
                .setParameter("boardId", boardId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count != null && count > 0;
    }

    public long countByBoard_Id(Integer boardId) {
        Long count = em.createQuery("""
                select count(bl)
                from BoardLike bl
                where bl.board.id = :boardId
                """, Long.class)
                .setParameter("boardId", boardId)
                .getSingleResult();
        return count == null ? 0L : count;
    }

    public Map<Integer, Long> countByBoardIds(List<Integer> boardIds) {
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
            likeCounts.put((Integer) row[0], (Long) row[1]);
        }
        return likeCounts;
    }

    @Transactional
    public int deleteByBoardId(Integer boardId) {
        return em.createQuery("""
                delete from BoardLike bl
                where bl.board.id = :boardId
                """)
                .setParameter("boardId", boardId)
                .executeUpdate();
    }

    @Transactional
    public int deleteByUserId(Integer userId) {
        return em.createQuery("""
                delete from BoardLike bl
                where bl.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Transactional
    public int deleteByBoardUserId(Integer userId) {
        return em.createQuery("""
                delete from BoardLike bl
                where bl.board.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
