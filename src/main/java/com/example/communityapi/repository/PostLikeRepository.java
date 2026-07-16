package com.example.communityapi.repository;

import com.example.communityapi.model.Post;
import com.example.communityapi.model.PostLike;
import com.example.communityapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 회원이 특정 게시글에 누른 좋아요 조회
    Optional<PostLike> findByUserAndPost(User user, Post post);
}