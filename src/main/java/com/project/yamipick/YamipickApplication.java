package com.project.yamipick;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class YamipickApplication {

    public static void main(String[] args) {
        SpringApplication.run(YamipickApplication.class, args);
    }

    // ★ 이 부분이 핵심! (서버 켜질 때 딱 한 번 실행됨)
    @PostConstruct
    public void init() {
        // 서버의 시간대를 '아시아/서울'로 강제 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("⏰ 서버 시간이 KST(Asia/Seoul)로 설정되었습니다.");
    }
}