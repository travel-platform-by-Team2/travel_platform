package com.example.travel_platform.community;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommunityRepository {

    private final EntityManager em;

    public CommunityPost savePost(CommunityPost post) {
        // TODO: 게시글 저장 처리
        return post;
    }

    public CommunityReply saveReply(CommunityReply reply) {
        // TODO: 댓글 저장 처리
        return reply;
    }

    public Optional<CommunityPost> findPostById(Integer postId) {
        // TODO: 게시글 단건 조회 구현
        return Optional.empty();
    }

    public List<CommunityPost> findPostList() {
        // TODO: 검색/정렬/페이징이 포함된 목록 조회 구현
        return List.of();
    }
}

