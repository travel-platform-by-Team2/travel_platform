package com.example.travel_platform.board;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Integer> {

    Optional<BoardLike> findByBoard_IdAndUser_Id(Integer boardId, Integer userId);

    boolean existsByBoard_IdAndUser_Id(Integer boardId, Integer userId);

    long countByBoard_Id(Integer boardId);

    @Modifying
    @Transactional
    @Query("""
            delete from BoardLike bl
            where bl.user.id = :userId
            """)
    int deleteByUserId(@Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("""
            delete from BoardLike bl
            where bl.board.user.id = :userId
            """)
    int deleteByBoardUserId(@Param("userId") Integer userId);
}
