package com.example.communityapi.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.communityapi.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    // Spring Security에서 사용할 사용자 조회 서비스
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // Spring Security에서 기존 MVC CORS 설정 사용
                .cors(cors -> {})

                // H2 Console iframe 허용
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )

                // Spring Security 기본 로그인 화면 사용 안 함
                .formLogin(form -> form.disable())

                // HTTP Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())

                // Spring Security 로그아웃 설정
                .logout(logout -> logout

                        // 로그아웃 요청 주소
                        .logoutUrl("/users/logout")

                        // 로그아웃 성공 시 200 응답 반환
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK)
                        )

                        // 로그아웃 시 세션 무효화
                        .invalidateHttpSession(true)

                        // SecurityContext 인증 정보 삭제
                        .clearAuthentication(true)

                        // 세션 쿠키 삭제
                        .deleteCookies("JSESSIONID")
                )

                // 요청별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // H2 Console 허용
                        .requestMatchers("/h2-console/**").permitAll()

                        // 회원가입, 로그인은 누구나 접근 가능
                        .requestMatchers(
                                "/users",
                                "/users/login",
                                "/users/logout"
                        ).permitAll()

                        // 그 외 요청은 로그인 필요
                        .anyRequest().authenticated()
                );
        // Security 설정 적용
        return http.build();
    }

    // 비밀번호 암호화를 위한 PasswordEncoder 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 사용자 인증을 처리하는 AuthenticationProvider 등록
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // 사용자 조회 서비스 등록
        provider.setUserDetailsService(customUserDetailsService);

        // 비밀번호 암호화 방식 등록
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // Spring Security 인증 관리자 등록
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();

    }
}