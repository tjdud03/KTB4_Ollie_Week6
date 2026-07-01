package com.example.communityapi.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateProfileRequest {

    private String nickname;

    private MultipartFile image;

}