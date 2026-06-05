package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.user.LoginRequest;
import com.example.communityapi.dto.user.SignupRequest;
import com.example.communityapi.dto.user.UpdatePasswordRequest;
import com.example.communityapi.dto.user.UpdateProfileRequest;
import com.example.communityapi.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class UserService {

    // 사용자 목록
    private final List <User> users = new ArrayList<>() ;
    // 현재 로그인한 사용자
    private User loginUser;

    // 회원가입
    public ResponseEntity<ApiResponse> signup(SignupRequest signupRequest){
        // 이메일 중복 검사
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(signupRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("email_already_exists", null));
            }
        }

        // 비밀번호 확인 불일치
        if (!signupRequest.getPassword().equals(signupRequest.getPasswordConfirm())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("password_mismatch", null));
        }

        User user = new User();

        user.setId(Long.valueOf(users.size() + 1));
        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setNickname(signupRequest.getNickname());

        users.add(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("register_success", null));
    }

    // 로그인
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest) {

        // 요청값 검사
        if (loginRequest.getEmail() == null
                || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null
                || loginRequest.getPassword().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        // 이메일, 비밀번호 일치 여부 확인
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equals(loginRequest.getEmail())
                    && users.get(i).getPassword().equals(loginRequest.getPassword())) {
                loginUser = users.get(i);

                return ResponseEntity.ok(
                        new ApiResponse("login_success", null));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("invalid_email_or_password", null));
    }

    // 회원정보 수정
    public ResponseEntity<ApiResponse> updateProfile(UpdateProfileRequest updateprofileRequest) {

        // 로그인 여부 확인
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("unauthorized", null));
        }

        loginUser.setNickname(updateprofileRequest.getNickname());
        loginUser.setProfileImage(updateprofileRequest.getProfileImage());

        return ResponseEntity.ok(
                new ApiResponse("update_profile_success", null)
        );
    }

    // 비밀번호 수정
    public ResponseEntity<ApiResponse> updatePassword(UpdatePasswordRequest updatepasswordRequest) {

        // 로그인 여부 확인
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("unauthorized", null));
        }

        // 비밀번호 유효성 검사
        if (!updatepasswordRequest.getPassword().matches(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$"
        )) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_password_format", null));
        }

        // 비밀번호 확인 불일치
        if (!updatepasswordRequest.getPassword().equals(updatepasswordRequest.getPasswordConfirm())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("password_mismatch", null));
        }

        // 기존 비밀번호와 동일
        if (loginUser.getPassword().equals(updatepasswordRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("same_as_old_password", null));
        }

        loginUser.setPassword(updatepasswordRequest.getPassword());

        return ResponseEntity.ok(
                new ApiResponse("update_password_success", null));
    }

    // 회원 탈퇴
    public ResponseEntity<ApiResponse> deleteUser() {

        // 로그인 여부 확인
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 로그인한 사용자 삭제
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(loginUser.getId())) {
                users.remove(i);
                loginUser = null;

                return ResponseEntity.ok(
                        new ApiResponse("delete_user_success", null));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse("user_not_found", null));
    }
}
