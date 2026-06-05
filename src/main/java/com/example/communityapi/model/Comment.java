package com.example.communityapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {

    private Long id;
    private Long postId;
    private String content;
}