package com.example.communityapi.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String password;
    private String passwordConfirm;
    private String email;
    private String nickname;
}