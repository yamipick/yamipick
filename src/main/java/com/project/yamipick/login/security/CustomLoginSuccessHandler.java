package com.project.yamipick.login.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.project.yamipick.login.dto.SessionUserDTO;
import com.project.yamipick.waiting.domain.WaitingMember;
import com.project.yamipick.waiting.domain.WaitingStore;
import com.project.yamipick.waiting.repository.WaitingStoreRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final WaitingStoreRepository storeRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // 1. 인증 정보에서 회원 꺼내기
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        WaitingMember member = principal.getMember();

        // 2. ★ [핵심 수정] 권한 체크 로직 유연화
        WaitingStore store = null;
        String role = member.getRole(); // DB값 (예: "ROLE_STORE")

        // "STORE"라는 글자가 포함되어 있거나, 관리자("ADMIN")라면 매장 조회 시도
        if (role != null && (role.contains("STORE") || role.contains("ADMIN"))) {
            store = storeRepository.findByOwnerId(member.getId()).orElse(null);
        }

        // 3. 세션용 DTO 생성
        SessionUserDTO sessionDto = new SessionUserDTO(member, store);

        // 4. 세션에 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", sessionDto);
        session.setMaxInactiveInterval(3600); // 1시간 유지

        // 5. 페이지 이동 (매장이 있으면 관리페이지, 없으면 유저페이지)
        if (sessionDto.isHasStore()) {
            response.sendRedirect("/"); 
        } else {
            response.sendRedirect("/"); 
        }
    }
}
