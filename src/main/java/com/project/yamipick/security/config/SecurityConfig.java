package com.project.yamipick.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.project.yamipick.security.handler.CustomLoginFailureHandler;
import com.project.yamipick.security.handler.CustomLoginSuccessHandler; // ★ import 확인

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // ★ 이거 꼭 있어야 함 (final 필드 자동 주입)
public class SecurityConfig {

    // ★ 방금 만든 핸들러를 주입받습니다.
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomLoginFailureHandler customLoginFailureHandler;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/img/**", "/upload/**").permitAll()
            .requestMatchers("/", "/login", "/join", "/joinok").permitAll()
            .requestMatchers("/api/log/**").permitAll()
            .requestMatchers("/ai/recommend", "/api/ai/recommend/**").permitAll()
            .requestMatchers("/ai/chatbot", "/api/chat/send").authenticated()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().permitAll() // 개발 끝나면 authenticated()로 변경!
        );

        http.formLogin(login -> login
            .loginPage("/login")
            .loginProcessingUrl("/loginProc")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler(customLoginSuccessHandler) 
            .failureHandler(customLoginFailureHandler)
            .permitAll()
        );

        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
        );

        return http.build();
    }
}