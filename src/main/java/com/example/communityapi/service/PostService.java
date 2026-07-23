package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.post.CreatePostRequest;
import com.example.communityapi.dto.post.UpdatePostRequest;
import com.example.communityapi.model.Post;
import com.example.communityapi.model.PostLike;
import com.example.communityapi.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import com.example.communityapi.repository.PostLikeRepository;
import com.example.communityapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    // 게시글 데이터 접근
    private final PostRepository postRepository;

    // 게시글 좋아요 데이터 접근
    private final PostLikeRepository postLikeRepository;

    private final UserService userService;

    // 게시글 작성
    public ResponseEntity<ApiResponse> createPost(CreatePostRequest createpostRequest) {

        // 제목, 내용 입력 여부 확인
        if (createpostRequest.getTitle() == null
                || createpostRequest.getTitle().isBlank()
                || createpostRequest.getContent() == null
                || createpostRequest.getContent().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        Post post = new Post();

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 게시글 작성자 설정
        post.setUser(currentUser);

        post.setTitle(createpostRequest.getTitle());
        post.setContent(createpostRequest.getContent());
        String imageUrl = null;

        if (createpostRequest.getImage() != null
                && !createpostRequest.getImage().isEmpty()) {

            String fileName =
                    UUID.randomUUID() + "_" +
                            createpostRequest.getImage().getOriginalFilename();

            String uploadPath = System.getProperty("user.dir") + "/uploads";

            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            System.out.println(uploadDir.getAbsolutePath());
            try {

                createpostRequest.getImage().transferTo(
                        new File(uploadDir, fileName));

                imageUrl = "/uploads/" + fileName;

            } catch (IOException e) {

                throw new RuntimeException(e);

            }

        }

        post.setImageUrl(imageUrl);
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setCommentCount(0);

        // 게시글 생성 및 수정 시간 저장
        LocalDateTime now = LocalDateTime.now();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        // 게시글 저장
        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("create_success", null));
    }

    // 게시글 목록 조회
    public ResponseEntity<ApiResponse> getPostList() {

        // 저장된 전체 게시글 조회
        return ResponseEntity.ok(
                new ApiResponse("get_posts_success", postRepository.findAll()));
    }

    // 게시글 상세 조회
    // countView가 false면 조회수를 올리지 않음 (수정 페이지에서 기존 내용만 불러올 때 사용)
    public ResponseEntity<ApiResponse> getPostDetail(Long id, boolean countView) {

        // 게시글 조회
        Optional<Post> optionalPost = postRepository.findById(id);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

        if (countView) {

            // 조회수 증가
            post.setViewCount(post.getViewCount() + 1);
            post.setUpdatedAt(LocalDateTime.now());

            // 변경된 조회수 저장
            postRepository.save(post);

        }

        // 로그인한 회원이 이 게시글에 좋아요를 눌렀는지 여부 설정
        User currentUser = userService.getCurrentUser();

        if (currentUser != null) {
            post.setLiked(
                    postLikeRepository.findByUserAndPost(currentUser, post).isPresent());
        }

        return ResponseEntity.ok(
                new ApiResponse("get_post_success", post));
    }

    // 게시글 수정
    public ResponseEntity<ApiResponse> updatePost(
            Long id,
            UpdatePostRequest updatepostRequest) {

        // 제목, 내용 입력 여부 확인
        if (updatepostRequest.getTitle() == null
                || updatepostRequest.getTitle().isBlank()
                || updatepostRequest.getContent() == null
                || updatepostRequest.getContent().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        // 수정할 게시글 조회
        Optional<Post> optionalPost = postRepository.findById(id);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 게시글 작성자와 현재 로그인한 회원이 다른 경우 수정 거부
        if (!post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("forbidden", null));
        }

        // 게시글 내용 수정
        post.setTitle(updatepostRequest.getTitle());
        post.setContent(updatepostRequest.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        // 수정 내용 저장
        postRepository.save(post);

        return ResponseEntity.ok(
                new ApiResponse("update_success", null));
    }

    // 게시글 삭제
    public ResponseEntity<ApiResponse> deletePost(Long id) {

        // 삭제할 게시글 조회
        Optional<Post> optionalPost = postRepository.findById(id);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 게시글 작성자와 현재 로그인한 회원이 다른 경우 삭제 거부
        if (!post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("forbidden", null));
        }

        // 게시글 삭제
        postRepository.delete(post);

        return ResponseEntity.ok(
                new ApiResponse("delete_post_success", null)
        );
    }

    // 게시글 좋아요 (이미 누른 상태면 좋아요 취소)
    public ResponseEntity<ApiResponse> likePost(Long postId) {

        // 게시글 조회
        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = userService.getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 현재 회원이 이미 이 게시글에 좋아요를 눌렀는지 조회
        Optional<PostLike> existingLike =
                postLikeRepository.findByUserAndPost(currentUser, post);

        if (existingLike.isPresent()) {

            // 이미 눌렀다면 좋아요 취소
            postLikeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);

            return ResponseEntity.ok(
                    new ApiResponse("like_cancel_success", null));
        }

        // 좋아요 기록 생성
        PostLike postLike = new PostLike();

        postLike.setUser(currentUser);
        postLike.setPost(post);
        postLike.setCreatedAt(LocalDateTime.now());

        postLikeRepository.save(postLike);

        // 좋아요 수 증가
        post.setLikeCount(post.getLikeCount() + 1);
        post.setUpdatedAt(LocalDateTime.now());

        // 변경 내용 저장
        postRepository.save(post);

        return ResponseEntity.ok(
                new ApiResponse("like_success", null));
    }

    // 게시글 존재 여부 확인
    public boolean existsPost(Long postId) {
        return postRepository.existsById(postId);
    }

    // 게시글 조회
    public Optional<Post> findPost(Long postId) {
        return postRepository.findById(postId);
    }
}