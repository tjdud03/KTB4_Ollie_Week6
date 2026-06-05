package com.example.communityapi.controller;

import com.example.communityapi.dto.comment.CreateCommentRequest;
import com.example.communityapi.dto.comment.UpdateCommentRequest;
import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    // 댓글 관련 비즈니스 로직 호출
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<ApiResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest createcommentRequest) {

        return commentService.createComment(postId, createcommentRequest);
    }

    // 게시글의 댓글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getCommentList(
            @PathVariable Long postId) {

        return commentService.getCommentList(postId);
    }

    // 게시글의 특정 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest updatecommentRequest) {

        return commentService.updateComment(
                postId,
                commentId,
                updatecommentRequest
        );
    }

    // 게시글의 특정 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        return commentService.deleteComment(
                postId,
                commentId
        );
    }
}