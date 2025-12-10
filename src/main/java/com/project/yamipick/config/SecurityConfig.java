package com.project.yamipick.config; // [체크] 본인 패키지명 확인

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.project.yamipick.login.security.CustomLoginSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
//로그인
@RequiredArgsConstructor
public class SecurityConfig {
	
	//로그인
	private final CustomLoginSuccessHandler successHandler; 

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
            .requestMatchers("/waiting/**", "/ws/waiting/**", "/user", "/store").permitAll()
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
            //.defaultSuccessUrl("/", true)     // 로그인 성공 시 메인으로 이동
            //로그인
            .successHandler(successHandler)
            .permitAll()
        );

        // (4) 로그아웃 설정
        http.logout(logout -> logout
            .logoutUrl("/logout")            // 로그아웃 요청 주소
            .logoutSuccessUrl("/")           // 로그아웃 후 메인으로 이동
            .invalidateHttpSession(true)     // 세션 삭제
        );

        return http.build(); // [중요] 설정을 마무리하고 반환 (이게 빠져서 에러 났던 것)
    }
}