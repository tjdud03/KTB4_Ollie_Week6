package com.example.communityapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_likes", // post_likes 테이블과 매핑
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"})
)
@Getter
@Setter
public class PostLike {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 ID 자동 생성
    private Long id;

    // 좋아요를 누른 사용자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 좋아요가 눌린 게시글
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // 좋아요를 누른 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;
}