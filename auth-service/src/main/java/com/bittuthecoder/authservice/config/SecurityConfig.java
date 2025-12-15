package com.bittuthecoder.authservice.config;

import com.bittuthecoder.authservice.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oauth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ❌ DO NOT force authentication here
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/auth/oauth2/success",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/**"   // ✅ IMPORTANT
                        ).permitAll()
                        .anyRequest().permitAll() // 🔥 KEY CHANGE
                )

                // OAuth2 ONLY for browser login
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(u -> u.userService(oauth2UserService))
                        .defaultSuccessUrl("/api/auth/oauth2/success", true)
                );

        return http.build();
    }
}
