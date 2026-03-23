package com.example.travel_platform.board;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
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
}
