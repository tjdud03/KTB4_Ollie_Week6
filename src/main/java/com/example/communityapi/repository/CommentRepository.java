package com.example.communityapi.repository;

import com.example.communityapi.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 댓글 목록 조회
    List<Comment> findByPost_Id(Long postId);

}