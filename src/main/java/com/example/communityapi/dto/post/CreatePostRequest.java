package com.example.communityapi.dto.post;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreatePostRequest {

    private String title;
    private String content;
    private MultipartFile image;

}