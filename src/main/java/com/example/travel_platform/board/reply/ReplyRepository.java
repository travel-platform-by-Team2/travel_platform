package com.example.travel_platform.board.reply;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReplyRepository {

    private final EntityManager em;

    public Reply save(Reply reply) {
        if (reply.getId() == null) {
            em.persist(reply);
            return reply;
        }
        return em.merge(reply);
    }

    public Optional<Reply> findById(Integer replyId) {
        return Optional.ofNullable(em.find(Reply.class, replyId));
    }

    public List<Reply> findByBoardId(Integer boardId) {
        return em.createQuery("""
                select r
                from Reply r
                join fetch r.user
                where r.board.id = :boardId
                order by r.id asc
                """, Reply.class)
                .setParameter("boardId", boardId)
                .getResultList();
    }

    public void delete(Reply reply) {
        em.remove(reply);
    }

    public int deleteByUserId(Integer userId) {
        return em.createQuery("""
                delete from Reply r
                where r.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public int deleteByBoardUserId(Integer userId) {
        return em.createQuery("""
                delete from Reply r
                where r.board.user.id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
