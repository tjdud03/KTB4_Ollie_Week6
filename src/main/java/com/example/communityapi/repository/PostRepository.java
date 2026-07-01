package com.example.communityapi.repository;

import com.example.communityapi.model.Post;
import com.example.communityapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 특정 회원의 게시글 전체 삭제
    void deleteAllByUser(User user);

}