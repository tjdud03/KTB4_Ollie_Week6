package com.example.communityapi.controller;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.post.CreatePostRequest;
import com.example.communityapi.dto.post.UpdatePostRequest;
import com.example.communityapi.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    // 게시글 관련 비즈니스 로직 호출
    private final PostService postService;

    // 게시글 작성
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> createPost(
            @ModelAttribute CreatePostRequest createpostRequest) {

        return postService.createPost(createpostRequest);
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getPostList() {

        return postService.getPostList();
    }

    // 게시글 상세 조회
    // countView=false로 호출하면 조회수를 올리지 않음 (수정 페이지에서 기존 내용만 불러올 때 사용)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPostDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean countView) {

        return postService.getPostDetail(id, countView);
    }

    // 게시글 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest updatepostRequest) {

        return postService.updatePost(id, updatepostRequest);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePost(
            @PathVariable Long id) {

        return postService.deletePost(id);
    }

    // 게시글 좋아요
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse> likePost(
            @PathVariable Long postId) {

        return postService.likePost(postId);
    }
}