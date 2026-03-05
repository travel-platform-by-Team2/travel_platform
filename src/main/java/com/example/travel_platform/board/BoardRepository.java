package com.example.travel_platform.board;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

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
}
