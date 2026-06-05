package com.example.communityapi.service;

import com.example.communityapi.dto.comment.CreateCommentRequest;
import com.example.communityapi.dto.comment.UpdateCommentRequest;
import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    // 댓글 목록
    private final List<Comment> comments = new ArrayList<>();

    // 게시글 존재 여부 확인용
    private final PostService postService;

    // 댓글 작성
    public ResponseEntity<ApiResponse> createComment(
            Long postId,
            CreateCommentRequest createcommentRequest) {

        // 댓글 내용 입력 여부 확인
        if (createcommentRequest.getContent() == null
                || createcommentRequest.getContent().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request",null));
        }

        // 게시글 존재 여부 확인
        if (!postService.existsPost(postId)) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Comment comment = new Comment();

        comment.setId(Long.valueOf(comments.size() + 1));
        comment.setPostId(postId);
        comment.setContent(createcommentRequest.getContent());

        comments.add(comment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("comment_create_success", null));
    }

    // 게시글의 댓글 목록 조회
    public ResponseEntity<ApiResponse> getCommentList(Long postId) {

        List<Comment> postComments = new ArrayList<>();

        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getPostId().equals(postId)) {
                postComments.add(comments.get(i));
            }
        }

        return ResponseEntity.ok(
                new ApiResponse("get_comments_success", postComments));
    }

    // 게시글의 특정 댓글 수정
    public ResponseEntity<ApiResponse> updateComment(
            Long postId,
            Long commentId,
            UpdateCommentRequest updatecommentRequest) {

        // 댓글 내용 입력 여부 확인
        if (updatecommentRequest.getContent() == null
                || updatecommentRequest.getContent().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)
                    && comments.get(i).getPostId().equals(postId)) {

                comments.get(i).setContent(
                        updatecommentRequest.getContent()
                );

                return ResponseEntity.ok(
                        new ApiResponse("comment_update_success", null)
                );
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("comment_not_found", null));
    }

    // 게시글의 특정 댓글 삭제
    public ResponseEntity<ApiResponse> deleteComment(
            Long postId,
            Long commentId) {

        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)
                    && comments.get(i).getPostId().equals(postId)) {

                comments.remove(i);

                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(new ApiResponse("no_content", null));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("comment_not_found", null));
    }
}
