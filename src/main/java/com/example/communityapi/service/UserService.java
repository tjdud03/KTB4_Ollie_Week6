package com.example.communityapi.service;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.user.LoginRequest;
import com.example.communityapi.dto.user.SignupRequest;
import com.example.communityapi.dto.user.UpdatePasswordRequest;
import com.example.communityapi.dto.user.UpdateProfileRequest;
import com.example.communityapi.model.User;
import com.example.communityapi.repository.CommentRepository;
import com.example.communityapi.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.communityapi.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class UserService {

    // 회원 데이터 접근
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // Spring Security 인증 관리자
    private final AuthenticationManager authenticationManager;

    // 비밀번호 암호화
    private final PasswordEncoder passwordEncoder;

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
    public ResponseEntity<ApiResponse> login(
            LoginRequest loginRequest, HttpServletRequest httpservletRequest) {

        // 요청값 검사
        if (loginRequest.getEmail() == null
                || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null
                || loginRequest.getPassword().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("invalid_request", null));
        }

        // Spring Security 인증 처리
        try {

            // 아이디와 비밀번호를 Authentication 객체로 생성
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.getEmail(),
                                    loginRequest.getPassword()
                            )
                    );

            // 비어있는 SecurityContext 생성
            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();

            // 인증 성공 정보를 SecurityContext에 저장
            securityContext.setAuthentication(authentication);

            // 현재 요청에서 사용할 SecurityContext 설정
            SecurityContextHolder.setContext(securityContext);

            // 다음 요청에서도 인증 상태를 유지할 수 있도록 세션에 SecurityContext 저장
            httpservletRequest.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            System.out.println("인증 성공 = " + authentication.getName());

            return ResponseEntity.ok(
                    new ApiResponse("login_success", null)
            );

        } catch (AuthenticationException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("invalid_email_or_password", null));
        }

    }

    // 회원정보 수정
    public ResponseEntity<ApiResponse> updateProfile(
            UpdateProfileRequest updateprofileRequest) {

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("unauthorized", null));
        }

        // 닉네임 수정
        currentUser.setNickname(updateprofileRequest.getNickname());

        // 새로운 프로필 이미지가 전달된 경우 이미지 저장
        if (updateprofileRequest.getImage() != null
                && !updateprofileRequest.getImage().isEmpty()) {

            try {

                // 파일명 중복 방지를 위해 UUID 사용
                String fileName =
                        java.util.UUID.randomUUID() + "_"
                                + updateprofileRequest.getImage().getOriginalFilename();

                // 이미지 저장 경로 설정
                String uploadPath =
                        System.getProperty("user.dir") + "/uploads";

                java.io.File uploadDir =
                        new java.io.File(uploadPath);

                // uploads 폴더가 없는 경우 생성
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // 이미지 파일 저장
                updateprofileRequest.getImage().transferTo(
                        new java.io.File(uploadDir, fileName)
                );

                // 변경된 프로필 이미지 경로 저장
                currentUser.setProfileImage("/uploads/" + fileName);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 회원정보 수정 시간 갱신
        currentUser.setUpdatedAt(LocalDateTime.now());

        // 변경된 회원 정보 저장
        userRepository.save(currentUser);

        return ResponseEntity.ok(
                new ApiResponse("update_profile_success", null)
        );
    }

    // 비밀번호 수정
    public ResponseEntity<ApiResponse> updatePassword(
            UpdatePasswordRequest updatepasswordRequest) {

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
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

        // 비밀번호와 비밀번호 확인 값이 일치하는지 검사
        if (!updatepasswordRequest.getPassword()
                .equals(updatepasswordRequest.getPasswordConfirm())) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("password_mismatch", null));
        }

        // 새 비밀번호가 기존 비밀번호와 동일한지 검사
        if (passwordEncoder.matches(
                updatepasswordRequest.getPassword(),
                currentUser.getPassword()
        )) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("same_as_old_password", null));
        }

        // 새 비밀번호를 암호화하여 저장
        currentUser.setPassword(
                passwordEncoder.encode(updatepasswordRequest.getPassword())
        );

        // 회원정보 수정 시간 갱신
        currentUser.setUpdatedAt(LocalDateTime.now());

        // 변경된 회원 정보 저장
        userRepository.save(currentUser);

        return ResponseEntity.ok(
                new ApiResponse("update_password_success", null)
        );
    }

    // 회원 탈퇴
    @Transactional
    public ResponseEntity<ApiResponse> deleteUser() {

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 현재 회원이 작성한 댓글 삭제
        commentRepository.deleteAllByUser(currentUser);

        // 현재 회원이 작성한 게시글 삭제
        postRepository.deleteAllByUser(currentUser);

        // 현재 회원 삭제
        userRepository.delete(currentUser);

        // 회원 탈퇴 후 SecurityContext의 인증 정보 초기화
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                new ApiResponse("delete_user_success", null)
        );
    }

    // 현재 로그인한 회원 정보 조회
    public ResponseEntity<ApiResponse> getLoginUserInfo() {

        // SecurityContext의 인증 정보를 기반으로 현재 로그인한 회원 조회
        User currentUser = getCurrentUser();

        // 인증된 회원이 없는 경우
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("login_required", null));
        }

        // 현재 로그인한 회원 정보 반환
        return ResponseEntity.ok(
                new ApiResponse("get_user_success", currentUser)
        );
    }

    // 현재 로그인한 회원 조회
    public User getCurrentUser() {

        // SecurityContext에 저장된 인증 정보 가져오기
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 인증된 회원이 없는 경우
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return null;
        }

        // 인증 정보에서 이메일 추출
        String email = authentication.getName();

        // 이메일을 기반으로 현재 회원 조회
        return userRepository.findByEmail(email)
                .orElse(null);
    }
}