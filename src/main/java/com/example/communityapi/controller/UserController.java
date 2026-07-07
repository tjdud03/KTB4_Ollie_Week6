package com.example.communityapi.controller;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.user.LoginRequest;
import com.example.communityapi.dto.user.SignupRequest;
import com.example.communityapi.dto.user.UpdatePasswordRequest;
import com.example.communityapi.dto.user.UpdateProfileRequest;
import com.example.communityapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    // 회원 관련 비즈니스 로직 호출
    private final UserService userService;

    // 회원가입
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> signup(
            @ModelAttribute SignupRequest signupRequest) {

        return userService.signup(signupRequest);

    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @RequestBody LoginRequest loginRequest, HttpServletRequest httpservletRequest) {

        return userService.login(loginRequest, httpservletRequest);
    }

    // 회원정보 수정
    @PatchMapping(value = "/profile", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> updateProfile(
            @ModelAttribute UpdateProfileRequest updateprofileRequest) {

        return userService.updateProfile(updateprofileRequest);
    }

    // 비밀번호 수정
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(
            @RequestBody UpdatePasswordRequest updatepasswordRequest) {

        return userService.updatePassword(updatepasswordRequest);
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteUser() {

        return userService.deleteUser();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getLoginUser() {

        return userService.getLoginUserInfo();

    }
}