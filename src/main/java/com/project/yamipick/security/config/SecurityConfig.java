package com.project.yamipick.security.config;

import com.project.yamipick.security.handler.CustomLoginSuccessHandler; // ★ import 확인
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // ★ 이거 꼭 있어야 함 (final 필드 자동 주입)
public class SecurityConfig {

    // ★ 방금 만든 핸들러를 주입받습니다.
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

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
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().permitAll() // 개발 끝나면 authenticated()로 변경!
        );

        http.formLogin(login -> login
            .loginPage("/login")
            .loginProcessingUrl("/loginProc")
            .usernameParameter("username")
            .passwordParameter("password")
            
            // ★ 여기가 핵심 변경 포인트! ★
            // 메서드 호출 방식(.successHandler(customSuccessHandler())) 대신
            // 주입받은 객체를 넣습니다.
            .successHandler(customLoginSuccessHandler) 
            
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