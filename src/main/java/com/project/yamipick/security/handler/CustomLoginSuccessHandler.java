package com.project.yamipick.security.handler; 

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.repository.BusinessLogRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor // ★ [추가] 이거 필수! (final 필드 자동 주입)
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    
    // ★ [추가] 로그 저장을 위해 리포지토리 가져오기
    private final BusinessLogRepository logRepository; 

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // ============================================================
        // ★ [추가된 부분] 로그인 로그 저장 (페이지 이동 전에 수행)
        // ============================================================
        try {
            String userId = authentication.getName();
            
            BusinessLog log = BusinessLog.builder()
                    .userId(userId)
                    .actionType("LOGIN")
                    .message("로그인 성공")
                    .ipAddr(request.getRemoteAddr())
                    .targetType("SYSTEM")
                    .createdAt(LocalDateTime.now())
                    .build();
            
            logRepository.save(log); // DB 저장
            
        } catch (Exception e) {
            e.printStackTrace(); // 로그 저장 실패해도 로그인은 계속 진행
        }
        // ============================================================


        // 1. 로그인 전에 가려던 페이지가 있는지 확인
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            System.out.println("👉 원래 가려던 페이지로 이동: " + targetUrl);
            response.sendRedirect(targetUrl);
            return; 
        }

        // 2. 가려던 페이지가 없다면? (권한별 리다이렉트)
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