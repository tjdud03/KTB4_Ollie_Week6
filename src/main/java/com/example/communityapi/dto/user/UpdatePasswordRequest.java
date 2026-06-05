package com.example.communityapi.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequest {

    private String password;
    private String passwordConfirm;
}