package com.example.communityapi.config;

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

    // 비밀번호 암호화 객체
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // H2 Console iframe 허용
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )

            // 요청별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth

                // H2 Console 접근 허용
                .requestMatchers("/h2-console/**").permitAll()

                // 모든 요청 허용
                .anyRequest().permitAll()
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
        provider.setPasswordEncoder(passwordEncoder);

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