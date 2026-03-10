package com.example.travel_platform.board;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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

    public void delete(Board board) {
        em.remove(board);
    }

    // 사용자가 게시글에 좋아요 눌렀는지 true/false 변환식
    public boolean existsLike(Integer boardId, Integer userId) {
        Number result = (Number) em.createNativeQuery("""
                select count(*)
                from board_like_tb
                where board_id = :boardId
                  and user_id = :userId
                """)
                .setParameter("boardId", boardId)
                .setParameter("userId", userId)
                .getSingleResult();
        return result.longValue() > 0;
    }

    // 게시글 총 좋아요 수
    public long countLike(Integer boardId) {
        Number result = (Number) em.createNativeQuery("""
                select count(*)
                from board_like_tb
                where board_id = :boardId
                """)
                .setParameter("boardId", boardId)
                .getSingleResult();
        return result.longValue();
    }

    @Transactional
    public int insertLike(Integer boardId, Integer userId) {
        return em.createNativeQuery("""
                insert into board_like_tb (board_id, user_id, created_at)
                values (:boardId, :userId, current_timestamp)
                """)
                .setParameter("boardId", boardId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Transactional
    public int deleteLike(Integer boardId, Integer userId) {
        return em.createNativeQuery("""
                delete from board_like_tb
                where board_id = :boardId
                  and user_id = :userId
                """)
                .setParameter("boardId", boardId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

}
