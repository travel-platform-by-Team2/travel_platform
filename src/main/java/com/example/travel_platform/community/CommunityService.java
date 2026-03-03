package com.example.travel_platform.community;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    @Transactional
    public void createPost(Integer sessionUserId, CommunityRequest.CreatePostDTO reqDTO) {
        // TODO: 작성 권한/입력값 검증
        // TODO: 엔티티 변환 후 저장
    }

    @Transactional
    public void updatePost(Integer sessionUserId, Integer postId, CommunityRequest.UpdatePostDTO reqDTO) {
        // TODO: 소유권 검증
        // TODO: 게시글 수정 처리
    }

    @Transactional
    public void deletePost(Integer sessionUserId, Integer postId) {
        // TODO: 소유권 검증
        // TODO: 게시글 삭제 처리
    }

    @Transactional
    public void createReply(Integer sessionUserId, Integer postId, CommunityRequest.CreateReplyDTO reqDTO) {
        // TODO: 게시글 존재 확인
        // TODO: 댓글 저장 처리
    }

    public List<CommunityResponse.PostSummaryDTO> getPostList() {
        // TODO: 목록 조회 + 필터/페이징 연동
        // TODO: PostSummaryDTO 매핑
        return List.of();
    }

    public CommunityResponse.PostDetailDTO getPostDetail(Integer postId) {
        // TODO: 상세 조회 + 조회수 증가 정책 반영
        // TODO: PostDetailDTO 매핑
        return null;
    }
}

