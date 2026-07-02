package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.user.LoginRequest;
import com.example.communityapi.dto.user.SignupRequest;
import com.example.communityapi.dto.user.UpdatePasswordRequest;
import com.example.communityapi.dto.user.UpdateProfileRequest;
import com.example.communityapi.model.User;
import com.example.communityapi.repository.CommentRepository;
import com.example.communityapi.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.communityapi.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    // 회원 데이터 접근
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

    // 현재 로그인한 사용자
    @Getter
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
        // 비밀번호 암호화
        user.setPassword(
                passwordEncoder.encode(signupRequest.getPassword())
        );
        user.setNickname(signupRequest.getNickname());

        // 회원가입 및 수정 시간 저장
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        String imageUrl = null;

        if (signupRequest.getImage() != null
                && !signupRequest.getImage().isEmpty()) {

            try {

                String fileName =
                        java.util.UUID.randomUUID() + "_"
                                + signupRequest.getImage().getOriginalFilename();

                String uploadPath = System.getProperty("user.dir") + "/uploads";

                java.io.File uploadDir = new java.io.File(uploadPath);

                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                signupRequest.getImage().transferTo(
                        new java.io.File(uploadDir, fileName)
                );

                imageUrl = "/uploads/" + fileName;

            } catch (Exception e) {

                throw new RuntimeException(e);

            }

        }

        user.setProfileImage(imageUrl);

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

        // 이메일로 회원 조회
        Optional<User> optionalUser =
                userRepository.findByEmail(loginRequest.getEmail());

        // 회원이 없는 경우
        if (optionalUser.isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("invalid_email_or_password", null));

        }

        User user = optionalUser.get();

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()
        )) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("invalid_email_or_password", null));

        }

        // 로그인 사용자 저장
        loginUser = user;

        System.out.println("UserService = " + this);
        System.out.println("loginUser = " + loginUser);

        return ResponseEntity.ok(
                new ApiResponse("login_success", null)
        );

    }

    // 회원정보 수정
    public ResponseEntity<ApiResponse> updateProfile(UpdateProfileRequest updateprofileRequest) {

        System.out.println(updateprofileRequest.getNickname());
        System.out.println(updateprofileRequest.getImage());

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("unauthorized", null));
        }

        loginUser.setNickname(updateprofileRequest.getNickname());

        if (updateprofileRequest.getImage() != null && !updateprofileRequest.getImage().isEmpty()) {

            try {

                String fileName =
                        java.util.UUID.randomUUID() + "_"
                                + updateprofileRequest.getImage().getOriginalFilename();

                String uploadPath = System.getProperty("user.dir") + "/uploads";

                java.io.File uploadDir = new java.io.File(uploadPath);

                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                updateprofileRequest.getImage().transferTo(
                        new java.io.File(uploadDir, fileName)
                );

                loginUser.setProfileImage("/uploads/" + fileName);

            } catch (Exception e) {

                throw new RuntimeException(e);

            }

        }

        loginUser.setUpdatedAt(LocalDateTime.now());

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
    @Transactional
    public ResponseEntity<ApiResponse> deleteUser() {

        // 로그인 여부 확인
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 내가 작성한 댓글 삭제
        commentRepository.deleteAllByUser(loginUser);

        // 내가 작성한 게시글 삭제
        postRepository.deleteAllByUser(loginUser);

        // 회원 삭제
        userRepository.delete(loginUser);

        // 로그인 사용자 정보 초기화
        loginUser = null;

        return ResponseEntity.ok(
                new ApiResponse("delete_user_success", null));
    }

    // 현재 로그인한 회원 조회
    public ResponseEntity<ApiResponse> getLoginUserInfo() {

        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        return ResponseEntity.ok(
                new ApiResponse("get_user_success", loginUser)
        );

    }
}