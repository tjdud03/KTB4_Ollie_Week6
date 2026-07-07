package com.example.communityapi.service;

import com.example.communityapi.dto.comment.CreateCommentRequest;
import com.example.communityapi.dto.comment.UpdateCommentRequest;
import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.model.Comment;
import com.example.communityapi.model.User;
import com.example.communityapi.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import com.example.communityapi.model.Post;

@Service
@RequiredArgsConstructor
public class CommentService {

    // 댓글 데이터 접근
    private final CommentRepository commentRepository;

    // 게시글 조회를 위한 Service
    private final PostService postService;

    private final UserService userService;

    // 댓글 작성
    public ResponseEntity<ApiResponse> createComment(
            Long postId,
            CreateCommentRequest createcommentRequest) {

        // 댓글 내용 입력 여부 확인
        if (createcommentRequest.getContent() == null
                || createcommentRequest.getContent().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        // 게시글 존재 여부 확인
        Optional<Post> optionalPost = postService.findPost(postId);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

        Comment comment = new Comment();

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 댓글 작성자 설정
        comment.setUser(currentUser);

        // 게시글과 댓글 연관관계 설정
        comment.setPost(post);
        comment.setContent(createcommentRequest.getContent());

        // 생성 및 수정 시간 저장
        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        // 댓글 저장
        commentRepository.save(comment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("comment_create_success", null));
    }

    // 게시글의 댓글 목록 조회
    public ResponseEntity<ApiResponse> getCommentList(Long postId) {

        return ResponseEntity.ok(
                new ApiResponse(
                        "get_comments_success",
                        // 게시글 ID에 해당하는 댓글 목록 조회
                        commentRepository.findByPost_Id(postId)
                )
        );
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

        // 댓글 조회
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("comment_not_found", null));
        }

        Comment comment = optionalComment.get();

        // 요청한 게시글의 댓글인지 확인
        if (!comment.getPost().getId().equals(postId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("comment_not_found", null));
        }

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 댓글 작성자와 현재 로그인한 회원이 다른 경우 수정 거부
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("forbidden", null));
        }

        // 댓글 내용 수정
        comment.setContent(updatecommentRequest.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        // 수정 내용 저장
        commentRepository.save(comment);

        return ResponseEntity.ok(
                new ApiResponse("comment_update_success", null));
    }

    // 게시글의 특정 댓글 삭제
    public ResponseEntity<ApiResponse> deleteComment(
            Long postId,
            Long commentId) {

        // 댓글 조회
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        if (optionalComment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("comment_not_found", null));
        }

        Comment comment = optionalComment.get();

        // 요청한 게시글의 댓글인지 확인
        if (!comment.getPost().getId().equals(postId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("comment_not_found", null));
        }

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 댓글 작성자와 현재 로그인한 회원이 다른 경우 삭제 거부
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("forbidden", null));
        }

        // 댓글 삭제
        commentRepository.delete(comment);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse("no_content", null));
    }
}
