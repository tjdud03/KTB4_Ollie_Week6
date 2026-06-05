package com.example.communityapi.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    private String nickname;
    private String profileImage;
}