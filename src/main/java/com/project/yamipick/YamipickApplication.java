package com.project.yamipick;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class YamipickApplication {

    public static void main(String[] args) {
        SpringApplication.run(YamipickApplication.class, args);
    }

    // ★ 이 부분 추가! (서버 켜질 때 '1234'의 진짜 암호문을 찍어줌)
    @Bean
    public CommandLineRunner getPassword(PasswordEncoder passwordEncoder) {
        return args -> {
            String pw = passwordEncoder.encode("1234");
            System.out.println("🔥 [복사하세요] 진짜 암호문: " + pw);
        };
    }
}