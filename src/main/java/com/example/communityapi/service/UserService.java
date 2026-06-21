package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.user.LoginRequest;
import com.example.communityapi.dto.user.SignupRequest;
import com.example.communityapi.dto.user.UpdatePasswordRequest;
import com.example.communityapi.dto.user.UpdateProfileRequest;
import com.example.communityapi.model.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.communityapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    // 회원 데이터 접근
    private final UserRepository userRepository;

    // 현재 로그인한 사용자
    private User loginUser;

    // 회원가입
    public ResponseEntity<ApiResponse> signup(SignupRequest signupRequest){

        // 요청값 검사
        if (signupRequest.getEmail() == null
                || signupRequest.getEmail().isBlank()
                || signupRequest.getPassword() == null
                || signupRequest.getPassword().isBlank()
                || signupRequest.getPasswordConfirm() == null
                || signupRequest.getPasswordConfirm().isBlank()
                || signupRequest.getNickname() == null
                || signupRequest.getNickname().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        // 이메일 중복 검사
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("email_already_exists", null));
        }

        // 비밀번호 확인 불일치
        if (!signupRequest.getPassword().equals(signupRequest.getPasswordConfirm())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("password_mismatch", null));
        }

        User user = new User();

        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setNickname(signupRequest.getNickname());

        // 회원가입 및 수정 시간 저장
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // 회원 정보 저장
        userRepository.save(user);

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

        // 이메일과 비밀번호가 일치하는 회원 조회
        Optional<User> optionalUser = userRepository.findByEmailAndPassword(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (optionalUser.isPresent()) {
            // 로그인 사용자 저장
            loginUser = optionalUser.get();

            return ResponseEntity.ok(
                    new ApiResponse("login_success", null));
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

        // 회원 정보 수정
        loginUser.setNickname(updateprofileRequest.getNickname());
        loginUser.setProfileImage(updateprofileRequest.getProfileImage());
        loginUser.setUpdatedAt(LocalDateTime.now());

        // 수정된 회원 정보 저장
        userRepository.save(loginUser);

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

        // 기존 비밀번호와 동일한지 확인
        if (loginUser.getPassword().equals(updatepasswordRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("same_as_old_password", null));
        }

        // 비밀번호 수정
        loginUser.setPassword(updatepasswordRequest.getPassword());
        loginUser.setUpdatedAt(LocalDateTime.now());

        // 변경된 회원 정보 저장
        userRepository.save(loginUser);

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

        // 회원 삭제
        userRepository.delete(loginUser);

        // 로그인 사용자 정보 초기화
        loginUser = null;

        return ResponseEntity.ok(
                new ApiResponse("delete_user_success", null));
    }
}