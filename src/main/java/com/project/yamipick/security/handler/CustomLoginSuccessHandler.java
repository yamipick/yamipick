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
import com.project.yamipick.user.entity.User;  // ✅ 추가
import com.project.yamipick.user.repository.UserRepository;  // ✅ 추가
import com.project.yamipick.waiting.domain.WaitingStore;  // ✅ 추가
import com.project.yamipick.waiting.dto.SessionUserDTO;
import com.project.yamipick.waiting.repository.WaitingStoreRepository;  // ✅ 추가

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;  // ✅ 추가
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final BusinessLogRepository logRepository;
    private final UserRepository userRepository;  // ✅ 추가
    private final WaitingStoreRepository storeRepository;  // ✅ 추가

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
    	
    	System.out.println(">>> CustomLoginSuccessHandler 호출됨, principal=" + authentication.getName());

        // ============================================================
        // 로그인 로그 저장 (기존 코드 유지)
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
            logRepository.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ============================================================
        // ✅ [추가] 웨이팅 시스템용 세션 저장
        // ============================================================
        try {
            String loginId = authentication.getName();
            User user = userRepository.findByUserId(loginId).orElse(null);
            
            if (user != null) {
                WaitingStore store = null;
                
                // 점주 권한(STORE, ADMIN)이 있는 경우에만 가게 정보 조회
                if (user.getRole().contains("STORE") || user.getRole().contains("ADMIN")) {
                    store = storeRepository.findByOwnerId(user.getSeqUser()).orElse(null);
                }

                // DTO 생성 (여기에 유저정보 + 가게정보 다 들어감)
                SessionUserDTO sessionDto = new SessionUserDTO(user, store);
                
                // 세션 가져오기
                HttpSession session = request.getSession();
                
                // ★ "waitingSession" 키 하나에 DTO 저장!
                session.setAttribute("waitingSession", sessionDto);
                
                // (확인용 로그)
                System.out.println(">>> [세션설정] waitingSession 저장 완료: " + sessionDto);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
            // 세션 저장 실패해도 로그인은 진행되도록 예외 처리
        }

        // 3. (생략) 페이지 리다이렉트 로직은 그대로 두세요.
    

        // ============================================================
        // 페이지 리다이렉트 (기존 코드 유지)
        // ============================================================
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            System.out.println("👉 원래 가려던 페이지로 이동: " + targetUrl);
            response.sendRedirect(targetUrl);
            return;
        }

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