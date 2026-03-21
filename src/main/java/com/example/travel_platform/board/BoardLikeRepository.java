package com.example.travel_platform.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Query("""
            select bl.board.id, count(bl)
            from BoardLike bl
            where bl.board.id in :boardIds
            group by bl.board.id
            """)
    List<Object[]> findLikeCountRowsByBoardIds(@Param("boardIds") List<Integer> boardIds);

    default Map<Integer, Long> countByBoardIds(List<Integer> boardIds) {
        if (boardIds == null || boardIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = findLikeCountRowsByBoardIds(boardIds);
        Map<Integer, Long> likeCounts = new HashMap<>();
        for (Object[] row : rows) {
            likeCounts.put((Integer) row[0], (Long) row[1]);
        }
        return likeCounts;
    }

    @Modifying
    @Transactional
    @Query("""
            delete from BoardLike bl
            where bl.board.id = :boardId
            """)
    int deleteByBoardId(@Param("boardId") Integer boardId);

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
