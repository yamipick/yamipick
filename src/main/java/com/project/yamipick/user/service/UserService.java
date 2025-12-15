package com.project.yamipick.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.user.dto.UserDTO;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 있는 암호화 기계

    @Transactional
    public void join(UserDTO dto) {
        // 1. 비밀번호 암호화 (1234 -> $2a$10$...)
        String encodedPwd = passwordEncoder.encode(dto.getPassword());

        // 2. Entity 생성 (Builder 사용)
        User user = User.builder()
                .userId(dto.getId())       // 주의: Entity 변수명이 userId인지 id인지 확인!
                .password(encodedPwd)      // 암호화된 비번 넣기
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .role("ROLE_USER")         // 가입하면 무조건 일반 유저
                .penaltyScore(0)           // "벌점은 0점부터 시작해"
                .statusUser("ACTIVE")      // 활동 상태
                .build();

        // 3. 저장
        userRepository.save(user);
    }
}