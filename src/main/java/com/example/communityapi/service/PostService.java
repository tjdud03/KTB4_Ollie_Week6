package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.post.CreatePostRequest;
import com.example.communityapi.dto.post.UpdatePostRequest;
import com.example.communityapi.model.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import com.example.communityapi.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    // 게시글 데이터 접근
    private final PostRepository postRepository;

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

        post.setTitle(createpostRequest.getTitle());
        post.setContent(createpostRequest.getContent());
        post.setImageUrl(createpostRequest.getImage());
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
    public ResponseEntity<ApiResponse> getPostDetail(Long id) {

        // 게시글 조회
        Optional<Post> optionalPost = postRepository.findById(id);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

        // 조회수 증가
        post.setViewCount(post.getViewCount() + 1);
        post.setUpdatedAt(LocalDateTime.now());

        // 변경된 조회수 저장
        postRepository.save(post);

        return ResponseEntity.ok(
                new ApiResponse("get_post_success", post));
    }

    // 게시글 수정
    public ResponseEntity<ApiResponse> updatePost(Long id, UpdatePostRequest updatepostRequest) {

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

        // 게시글 삭제
        postRepository.delete(optionalPost.get());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse("no_content", null));
    }

    // 게시글 좋아요
    public ResponseEntity<ApiResponse> likePost(Long postId) {

        // 게시글 조회
        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("post_not_found", null));
        }

        Post post = optionalPost.get();

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