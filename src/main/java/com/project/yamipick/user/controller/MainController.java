package com.project.yamipick.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/")
    @ResponseBody // HTML 파일 없어도 그냥 글자 띄우기
    public String index() {
        return "<h1>👋 안녕하세요! 여기는 Yamipick 메인입니다.</h1>" +
               "<p>일반 회원 로그인 성공!</p>" + 
               "<a href='/logout'>로그아웃</a>";
    }
}