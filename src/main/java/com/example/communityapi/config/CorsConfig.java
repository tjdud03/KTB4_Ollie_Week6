package com.example.communityapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {

                corsRegistry.addMapping("/**")
                            .allowedOrigins("http://localhost:5500")
                            .allowedMethods("*")
                            .allowedHeaders("*")
                            // 세션 쿠키를 포함한 교차 출처 요청 허용
                            .allowCredentials(true);
            }

        };

    }

}