package com.example.communityapi.dto.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    private String title;
    private String content;
    private String image;
}