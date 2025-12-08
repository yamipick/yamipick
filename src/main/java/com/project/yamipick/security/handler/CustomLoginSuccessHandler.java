package com.project.yamipick.security.handler; // 패키지명 알맞게 수정

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    // "사용자가 가려던 곳"을 기억해두는 캐시 (스프링 시큐리티 기본 기능)
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // 1. 로그인 전에 가려던 페이지가 있는지 확인 (캐시 뒤지기)
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) {
            // ★ 가려던 페이지가 있다면 -> 거기로 바로 이동!
            String targetUrl = savedRequest.getRedirectUrl();
            System.out.println("👉 원래 가려던 페이지로 이동: " + targetUrl);
            response.sendRedirect(targetUrl);
            return; // 여기서 끝냄
        }

        // 2. 가려던 페이지가 없다면? (그냥 로그인 버튼 눌러서 들어옴)
        // -> 기존처럼 권한별 리다이렉트 로직 수행
        var authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            System.out.println("👉 (기본) 관리자 대시보드로 이동");
            response.sendRedirect("/admin/dashboard");
        } else {
            System.out.println("👉 (기본) 메인 페이지로 이동");
            response.sendRedirect("/");
        }
    }
}