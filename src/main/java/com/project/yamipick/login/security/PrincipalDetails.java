package com.project.yamipick.login.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.yamipick.waiting.domain.WaitingMember;

import lombok.Getter;

@Getter
public class PrincipalDetails implements UserDetails {

    private final WaitingMember member;

    public PrincipalDetails(WaitingMember member) {
        this.member = member;
    }

    // ★ [핵심 수정] DB 데이터 상태에 따라 유동적으로 처리
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = member.getRole();
        if (role == null) role = "USER"; // 비어있으면 기본값

        // 이미 "ROLE_"로 시작하면 그대로 쓰고, 아니면 붙여줌
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword() { return member.getPassword(); }
    @Override public String getUsername() { return member.getLoginId(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
