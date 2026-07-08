package com.example.communityapi.user;

import com.example.communityapi.dto.common.ApiResponse;
import com.example.communityapi.dto.user.LoginRequest;
import com.example.communityapi.dto.user.SignupRequest;
import com.example.communityapi.dto.user.UpdatePasswordRequest;
import com.example.communityapi.dto.user.UpdateProfileRequest;
import com.example.communityapi.model.User;
import com.example.communityapi.repository.CommentRepository;
import com.example.communityapi.repository.PostRepository;
import com.example.communityapi.repository.UserRepository;
import com.example.communityapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    // 회원 데이터 접근 의존성
    @Mock
    private UserRepository userRepository;

    // 게시글 데이터 접근 의존성
    @Mock
    private PostRepository postRepository;

    // 댓글 데이터 접근 의존성
    @Mock
    private CommentRepository commentRepository;

    // Spring Security 인증 처리 의존성
    @Mock
    private AuthenticationManager authenticationManager;

    // 비밀번호 암호화 의존성
    @Mock
    private PasswordEncoder passwordEncoder;

    // 테스트 대상
    @InjectMocks
    private UserService userService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession httpSession;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile image;

    @AfterEach
    void tearDown() {
        // 로그인 성공 테스트에서 설정한 SecurityContext가
        // 다음 테스트에 영향을 주지 않도록 초기화
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signupSuccess() {

        // given
        SignupRequest signupRequest = new SignupRequest();

        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPasswordConfirm("Password123!");
        signupRequest.setNickname("테스트");

        // 이메일이 중복되지 않은 상황 설정 (Stub)
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

        // 비밀번호 암호화 결과 설정 (Stub)
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        // when
        ResponseEntity<ApiResponse> response = userService.signup(signupRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("register_success", response.getBody().getMessage());

        // 회원 저장 메소드가 한 번 호출되었는지 검증 (Mock)
        verify(userRepository, times(1)).save(any(User.class));

        // 비밀번호 암호화 메소드가 한 번 호출되었는지 검증 (Mock)
        verify(passwordEncoder, times(1)).encode("Password123!");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 실패한다")
    void signupFailDuplicateEmail() {

        // given
        SignupRequest signupRequest = new SignupRequest();

        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPasswordConfirm("Password123!");
        signupRequest.setNickname("테스트");

        // 이미 같은 이메일을 사용하는 회원이 존재하는 상황 설정 (Stub)
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        // when
        ResponseEntity<ApiResponse> response = userService.signup(signupRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("email_already_exists", response.getBody().getMessage());

        // 회원이 저장되지 않았는지 검증
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다르면 회원가입에 실패한다")
    void signupFailPasswordMismatch() {

        // given
        SignupRequest signupRequest = new SignupRequest();

        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("Password123!");
        signupRequest.setPasswordConfirm("Password456!");
        signupRequest.setNickname("테스트");

        // 이메일이 중복되지 않은 상황 설정 (Stub)
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

        // when
        ResponseEntity<ApiResponse> response = userService.signup(signupRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("password_mismatch", response.getBody().getMessage());

        // 회원이 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증 (Mock)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("필수값이 비어있으면 회원가입에 실패한다")
    void signupFailInvalidRequest() {

        // given
        SignupRequest signupRequest = new SignupRequest();

        signupRequest.setEmail("");
        signupRequest.setPassword("Password123!");
        signupRequest.setPasswordConfirm("Password123!");
        signupRequest.setNickname("테스트");

        // when
        ResponseEntity<ApiResponse> response = userService.signup(signupRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("invalid_request", response.getBody().getMessage());

        // 회원이 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증 (Mock)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("이메일 또는 비밀번호가 비어있으면 로그인에 실패한다")
    void loginFailInvalidRequest() {

        // given
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail("");
        loginRequest.setPassword("Password123!");

        // when
        ResponseEntity<ApiResponse> response = userService.login(loginRequest, httpServletRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("invalid_request", response.getBody().getMessage());

        // 인증 시도 자체가 없었는지 검증 (Mock)
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("이메일 또는 비밀번호가 틀리면 로그인에 실패한다")
    void loginFailInvalidCredentials() {

        // given
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("WrongPassword123!");

        // 인증 실패 상황 설정 (Stub)
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("인증 실패"));

        // when
        ResponseEntity<ApiResponse> response = userService.login(loginRequest, httpServletRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("invalid_email_or_password", response.getBody().getMessage());

        // 세션에 인증 정보가 저장되지 않았는지 검증 (Mock)
        verify(httpServletRequest, never()).getSession(true);
    }

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() {

        // given
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("Password123!");

        // 인증 성공 상황 설정 (Stub)
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        // 인증 객체가 반환할 사용자 이름 설정 (Stub)
        when(authentication.getName()).thenReturn("test@test.com");

        // 세션 생성 결과 설정 (Stub)
        when(httpServletRequest.getSession(true)).thenReturn(httpSession);

        // when
        ResponseEntity<ApiResponse> response = userService.login(loginRequest, httpServletRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("login_success", response.getBody().getMessage());

        // 세션에 SecurityContext가 저장되었는지 검증 (Mock)
        verify(httpSession, times(1)).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any(SecurityContext.class));

        // 인증 정보가 SecurityContext에 실제로 설정되었는지 검증
        assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("인증되지 않은 상태면 비밀번호 변경에 실패한다")
    void updatePasswordFailUnauthorized() {

        // given
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();

        updatePasswordRequest.setPassword("NewPassword123!");
        updatePasswordRequest.setPasswordConfirm("NewPassword123!");

        // 인증되지 않은 상황 설정
        SecurityContextHolder.clearContext();

        // when
        ResponseEntity<ApiResponse> response = userService.updatePassword(updatePasswordRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("unauthorized", response.getBody().getMessage());

        // 회원 정보가 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증 (Mock)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 형식이 올바르지 않으면 비밀번호 변경에 실패한다")
    void updatePasswordFailInvalidFormat() {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setPassword("encodedOldPassword");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();

        updatePasswordRequest.setPassword("short");
        updatePasswordRequest.setPasswordConfirm("short");

        // when
        ResponseEntity<ApiResponse> response = userService.updatePassword(updatePasswordRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("invalid_password_format", response.getBody().getMessage());

        // 회원 정보가 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증 (Mock)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호와 비밀번호 확인이 다르면 비밀번호 변경에 실패한다")
    void updatePasswordFailPasswordMismatch() {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setPassword("encodedOldPassword");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();

        updatePasswordRequest.setPassword("NewPassword123!");
        updatePasswordRequest.setPasswordConfirm("Different123!");

        // when
        ResponseEntity<ApiResponse> response = userService.updatePassword(updatePasswordRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("password_mismatch", response.getBody().getMessage());

        // 회원 정보가 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증 (Mock)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("새 비밀번호가 기존 비밀번호와 같으면 비밀번호 변경에 실패한다")
    void updatePasswordFailSameAsOldPassword() {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setPassword("encodedOldPassword");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();

        updatePasswordRequest.setPassword("NewPassword123!");
        updatePasswordRequest.setPasswordConfirm("NewPassword123!");

        // 새 비밀번호가 기존 비밀번호와 동일한 상황 설정 (Stub)
        when(passwordEncoder.matches("NewPassword123!", "encodedOldPassword")).thenReturn(true);

        // when
        ResponseEntity<ApiResponse> response = userService.updatePassword(updatePasswordRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("same_as_old_password", response.getBody().getMessage());

        // 회원 정보가 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));

        // 비밀번호 암호화도 실행되지 않았는지 검증 (Mock)
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccess() {

        // given (준비)
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setPassword("encodedOldPassword");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();

        updatePasswordRequest.setPassword("NewPassword123!");
        updatePasswordRequest.setPasswordConfirm("NewPassword123!");

        // 새 비밀번호가 기존 비밀번호와 다른 상황 설정 (Stub)
        when(passwordEncoder.matches("NewPassword123!", "encodedOldPassword")).thenReturn(false);

        // 비밀번호 암호화 결과 설정 (Stub)
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedNewPassword");

        // when
        ResponseEntity<ApiResponse> response = userService.updatePassword(updatePasswordRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("update_password_success", response.getBody().getMessage());

        // 회원 저장 메소드가 한 번 호출되었는지 검증 (Mock)
        verify(userRepository, times(1)).save(currentUser);

        // 비밀번호가 새로 암호화된 값으로 바뀌었는지 검증
        assertEquals("encodedNewPassword", currentUser.getPassword());
    }

    @Test
    @DisplayName("인증되지 않은 상태면 회원정보 수정에 실패한다")
    void updateProfileFailUnauthorized() {

        // given
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();

        updateProfileRequest.setNickname("새닉네임");

        // 인증되지 않은 상황 설정
        SecurityContextHolder.clearContext();

        // when
        ResponseEntity<ApiResponse> response = userService.updateProfile(updateProfileRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("unauthorized", response.getBody().getMessage());

        // 회원 정보가 저장되지 않았는지 검증 (Mock)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이미지 없이 회원정보 수정에 성공한다")
    void updateProfileSuccessWithoutImage() {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setNickname("기존닉네임");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();

        updateProfileRequest.setNickname("새닉네임");

        // when
        ResponseEntity<ApiResponse> response = userService.updateProfile(updateProfileRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("update_profile_success", response.getBody().getMessage());

        // 닉네임이 변경되었는지 검증
        assertEquals("새닉네임", currentUser.getNickname());

        // 이미지가 없으면 프로필 이미지는 그대로인지 검증
        assertNull(currentUser.getProfileImage());

        // 회원 정보가 저장되었는지 검증 (Mock)
        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    @DisplayName("이미지와 함께 회원정보 수정에 성공한다")
    void updateProfileSuccessWithImage() throws java.io.IOException {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setNickname("기존닉네임");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        // 업로드할 이미지 파일 상황 설정 (Stub)
        when(image.isEmpty()).thenReturn(false);
        when(image.getOriginalFilename()).thenReturn("profile.png");

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();

        updateProfileRequest.setNickname("새닉네임");
        updateProfileRequest.setImage(image);

        // when
        ResponseEntity<ApiResponse> response = userService.updateProfile(updateProfileRequest);

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("update_profile_success", response.getBody().getMessage());

        // 닉네임이 변경되었는지 검증
        assertEquals("새닉네임", currentUser.getNickname());

        // 프로필 이미지 경로가 업로드한 파일명으로 갱신되었는지 검증
        assertTrue(currentUser.getProfileImage().endsWith("_profile.png"));

        // 이미지 저장이 시도되었는지 검증 (Mock)
        verify(image, times(1)).transferTo(any(File.class));

        // 회원 정보가 저장되었는지 검증 (Mock)
        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    @DisplayName("인증되지 않은 상태면 회원 탈퇴에 실패한다")
    void deleteUserFailUnauthorized() {

        // given

        // 인증되지 않은 상황 설정
        SecurityContextHolder.clearContext();

        // when
        ResponseEntity<ApiResponse> response = userService.deleteUser();

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("login_required", response.getBody().getMessage());

        // 회원이 삭제되지 않았는지 검증 (Mock)
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUserSuccess() {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        // when
        ResponseEntity<ApiResponse> response = userService.deleteUser();

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("delete_user_success", response.getBody().getMessage());

        // 회원이 작성한 댓글이 삭제되었는지 검증 (Mock)
        verify(commentRepository, times(1)).deleteAllByUser(currentUser);

        // 회원이 작성한 게시글이 삭제되었는지 검증 (Mock)
        verify(postRepository, times(1)).deleteAllByUser(currentUser);

        // 회원이 삭제되었는지 검증 (Mock)
        verify(userRepository, times(1)).delete(currentUser);

        // 탈퇴 후 SecurityContext가 초기화되었는지 검증
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("인증되지 않은 상태면 로그인 회원 정보 조회에 실패한다")
    void getLoginUserInfoFailUnauthorized() {

        // given

        // 인증되지 않은 상황 설정
        SecurityContextHolder.clearContext();

        // when
        ResponseEntity<ApiResponse> response = userService.getLoginUserInfo();

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("login_required", response.getBody().getMessage());
    }

    @Test
    @DisplayName("로그인 회원 정보 조회 성공")
    void getLoginUserInfoSuccess() {

        // given
        User currentUser = new User();

        currentUser.setEmail("test@test.com");
        currentUser.setNickname("테스트");

        // 로그인된 회원 상황 설정
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@test.com");
        when(authentication.getName()).thenReturn("test@test.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증된 이메일로 회원이 조회되는 상황 설정 (Stub)
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(currentUser));

        // when
        ResponseEntity<ApiResponse> response = userService.getLoginUserInfo();

        // then

        // HTTP 상태 코드 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 응답 메시지 검증
        assertEquals("get_user_success", response.getBody().getMessage());

        // 응답 데이터로 현재 로그인한 회원 정보가 담겼는지 검증
        assertEquals(currentUser, response.getBody().getData());
    }
}
