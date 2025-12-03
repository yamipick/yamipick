package com.project.yamipick.security.config; // [체크] 본인 패키지명 확인

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 비밀번호 암호화 빈 (필수)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 필터 체인 (보안 설정의 핵심)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // (1) CSRF 설정 (개발 중에는 귀찮으니 끄기 / 운영 땐 켜는 게 좋음)
        http.csrf(csrf -> csrf.disable());

        // (2) 권한 설정 (누가 어디에 갈 수 있나)
        http.authorizeHttpRequests(auth -> auth
            // static 리소스(css, js, img)는 무조건 허용 (로그인 안 해도 보여야 함)
            .requestMatchers("/css/**", "/js/**", "/img/**", "/upload/**").permitAll()
            // 메인, 회원가입, 로그인 페이지는 모두 허용
            .requestMatchers("/", "/login", "/join", "/joinok").permitAll()
            // 로그 api 전역 허용
            .requestMatchers("/api/log/**").permitAll()
            // 관리자 페이지는 ADMIN 권한만 허용
            .requestMatchers("/admin/**").hasRole("ADMIN")
         // .anyRequest().authenticated() // (주석 처리) 나중에 개발 다 끝나면 이거 푸세요!
            .anyRequest().permitAll()        // (추가) 지금은 개발 중이니 모두 통과!
        );

        // (3) 로그인 설정 (Form Login 방식 - Thymeleaf에 적합)
        http.formLogin(login -> login
            .loginPage("/login")             // 우리가 만든 커스텀 로그인 페이지 사용
            .loginProcessingUrl("/loginProc") // HTML Form의 action 주소와 일치해야 함
            .usernameParameter("username")    // HTML input name="username"
            .passwordParameter("password")    // HTML input name="password"
            .successHandler(customSuccessHandler())
            .permitAll()
        );

        // (4) 로그아웃 설정
        http.logout(logout -> logout
            .logoutUrl("/logout")            // 로그아웃 요청 주소
            .logoutSuccessUrl("/")           // 로그아웃 후 메인으로 이동
            .invalidateHttpSession(true)     // 세션 삭제
        );

        return http.build(); // [중요] 설정을 마무리하고 반환
    }
    
    // 로그인 성공 핸들러
    @Bean
    AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            // 1. 로그인한 사람의 권한 목록 가져오기
            var authorities = authentication.getAuthorities();
            
            // ★ [디버깅] 콘솔에 권한 찍어보기 (이게 중요!)
            System.out.println("🔥 로그인 성공! 현재 권한: " + authorities);

            // 2. 권한 확인 (ROLE_ADMIN이 있는지?)
            boolean isAdmin = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                System.out.println("👉 관리자 페이지로 이동합니다.");
                response.sendRedirect("/admin/notice/list");
            } else {
                System.out.println("👉 일반 메인으로 이동합니다.");
                response.sendRedirect("/");
            }
        };
    }
}