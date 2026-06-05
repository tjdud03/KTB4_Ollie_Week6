package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.post.CreatePostRequest;
import com.example.communityapi.dto.post.UpdatePostRequest;
import com.example.communityapi.model.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    // 게시글 목록
    private final List<Post> posts = new ArrayList<>();

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

        post.setId(Long.valueOf(posts.size() + 1));
        post.setTitle(createpostRequest.getTitle());
        post.setContent(createpostRequest.getContent());
        post.setImage(createpostRequest.getImage());
        post.setLikeCount(0);
        post.setViewCount(0);
        post.setCommentCount(0);

        posts.add(post);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("create_success", null));
    }

    // 게시글 목록 조회
    public ResponseEntity<ApiResponse> getPostList() {
        return ResponseEntity.ok(
                new ApiResponse("get_posts_success", posts));
    }
    // 게시글 상세 조회
    public ResponseEntity<ApiResponse> getPostDetail(Long id) {

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(id)) {
                posts.get(i).setViewCount(
                        posts.get(i).getViewCount() + 1
                );

                return ResponseEntity.ok(
                        new ApiResponse("get_post_success", posts.get(i)));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("post_not_found", null));
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

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            if (post.getId().equals(id)) {

                post.setTitle(updatepostRequest.getTitle());
                post.setContent(updatepostRequest.getContent());

                return ResponseEntity.ok(
                        new ApiResponse("update_success", null));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("post_not_found", null));
    }

    // 게시글 삭제
    public ResponseEntity<ApiResponse> deletePost(Long id) {

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(id)) {
                posts.remove(i);
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(new ApiResponse("no_content", null));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("post_not_found", null));
    }

    // 게시글 좋아요
    public ResponseEntity<ApiResponse> likePost(Long postId) {

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {

                // 좋아요 수 증가
                posts.get(i).setLikeCount(
                        posts.get(i).getLikeCount() + 1);

                return ResponseEntity.ok(
                        new ApiResponse("like_success", null));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("post_not_found", null));
    }

    // 게시글 존재여부 확인
    public boolean existsPost(Long postId) {
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                return true;
            }
        }

        return false;
    }
}