package com.example.travel_platform.board;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Integer> {

    Optional<BoardLike> findByBoard_IdAndUser_Id(Integer boardId, Integer userId);

    boolean existsByBoard_IdAndUser_Id(Integer boardId, Integer userId);

    long countByBoard_Id(Integer boardId);
}
