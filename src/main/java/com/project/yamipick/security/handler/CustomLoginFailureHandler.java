package com.project.yamipick.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage = "아이디 또는 비밀번호를 확인해주세요.";

        // 예외 종류별로 메시지 다르게 보여주기
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.";
        } else if (exception instanceof LockedException) {
            // 아까 Service에서 던진 "정지된 계정입니다..." 메시지 받기
            errorMessage = exception.getMessage(); 
        } else if (exception instanceof DisabledException) {
            errorMessage = "탈퇴 처리된 계정입니다.";
        }

        // 한글 깨짐 방지
        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");
        
        // 로그인 페이지로 다시 보내면서 에러 메시지 달아주기
        setDefaultFailureUrl("/login?error=true&exception=" + errorMessage);
        
        super.onAuthenticationFailure(request, response, exception);
    }
}