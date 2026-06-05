package com.example.communityapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Post {

    private Long id;
    private String title;
    private String content;
    private String image;
    private int likeCount;
    private Integer viewCount;
    private Integer commentCount;
}