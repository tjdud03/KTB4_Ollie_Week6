package com.example.communityapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // users 테이블과 매핑
@Getter
@Setter
public class User {

    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 ID 자동 생성
    private Long id;

    // 이메일
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 비밀번호
    @Column(nullable = false, length = 255)
    private String password;

    // 닉네임
    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    // 프로필 이미지 URL
    @Column(length = 255)
    private String profileImage;

    // 회원가입 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 회원정보 수정 시간
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}