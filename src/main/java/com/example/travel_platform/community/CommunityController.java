package com.example.travel_platform.community;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public Object getPostList() {
        // TODO: 검색/정렬/페이지 파라미터 수집
        return communityService.getPostList();
    }

    @GetMapping("/{postId}")
    public Object getPostDetail(@PathVariable Integer postId) {
        // TODO: 상세 조회 응답 스펙 확정
        return communityService.getPostDetail(postId);
    }

    @PostMapping
    public void createPost(@Valid @RequestBody CommunityRequest.CreatePostDTO reqDTO) {
        // TODO: 세션 사용자 식별값 연동
        communityService.createPost(1, reqDTO);
    }

    @PutMapping("/{postId}")
    public void updatePost(@PathVariable Integer postId, @Valid @RequestBody CommunityRequest.UpdatePostDTO reqDTO) {
        // TODO: 세션 사용자 식별값 연동
        communityService.updatePost(1, postId, reqDTO);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Integer postId) {
        // TODO: 세션 사용자 식별값 연동
        communityService.deletePost(1, postId);
    }

    @PostMapping("/{postId}/replies")
    public void createReply(@PathVariable Integer postId, @Valid @RequestBody CommunityRequest.CreateReplyDTO reqDTO) {
        // TODO: 세션 사용자 식별값 연동
        communityService.createReply(1, postId, reqDTO);
    }
}

