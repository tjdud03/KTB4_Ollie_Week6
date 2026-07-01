package com.example.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts") // posts 테이블과 매핑
@Getter
@Setter
public class Post {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 ID 자동 생성
    private Long id;

    // 게시글 작성자
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 게시글 제목
    @Column(nullable = false, length = 30)
    private String title;

    // 게시글 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 게시글 이미지 URL
    @Column(length = 255)
    private String imageUrl;

    // 조회수
    @Column(nullable = false)
    private Integer viewCount;

    // 좋아요 수
    @Column(nullable = false)
    private Integer likeCount;

    // 댓글 수
    @Column(nullable = false)
    private Integer commentCount;

    // 생성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<Comment> comments;
}