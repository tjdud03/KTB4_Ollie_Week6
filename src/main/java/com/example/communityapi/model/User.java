package com.example.communityapi.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class User {

    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
}