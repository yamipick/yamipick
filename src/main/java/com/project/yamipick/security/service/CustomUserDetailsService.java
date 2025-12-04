package com.project.yamipick.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        log.info("로그인 시도 아이디 {} ", username );
        
        // 1. DB에서 아이디로 회원 찾기
        User userData = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));
        
        // 2. 상태값 검사 (로그인 차단 로직)
        String status = userData.getStatusUser();
        
        if ("WITHDRAWN".equals(status)) {
            throw new UsernameNotFoundException("탈퇴한 회원입니다.");
        }
        
        // ★ [추가] BLIND 상태면 로그인 막기
        if ("BLIND".equals(status) || "SUSPENDED".equals(status)) { 
             // "정지"나 "블라인드"나 사용자는 못 들어오게 막음
            throw new UsernameNotFoundException("정지된 계정입니다. 관리자에게 문의하세요.");
        }
        
        log.info("✅ 회원 확인됨: {} (권한: {})", userData.getUserId(), userData.getRole());
        
        // 3. 시큐리티에게 넘겨주기
        return org.springframework.security.core.userdetails.User.builder()
                .username(userData.getUserId())
                .password(userData.getPassword())
                .roles(userData.getRole().replace("ROLE_", ""))
                .build();
    }
}