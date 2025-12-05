package com.project.yamipick.reservation.auth;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.yamipick.user.dto.UserDTO;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    public void signup(UserDTO dto) {

        // 아이디 중복 체크 (User.userId 기준)
        Optional<User> existing = userRepository.findByUserId(dto.getId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 암호화
        String encPw = passwordEncoder.encode(dto.getPassword());

        // User 엔티티 빌더 사용 (setter 없음!)
        User user = User.builder()
                .name(dto.getName())
                .userId(dto.getId())            // ★ dto.id → entity.userId
                .password(encPw)
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .role(dto.getRole())            // "CUSTOMER" / "STORE" / "ADMIN"
                .penaltyScore(0)
                .statusUser("ACTIVE")
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인 검증
     *  - 성공: User 리턴
     *  - 실패: null 리턴
     */
    public User login(String userId, String rawPassword) {

        Optional<User> opt = userRepository.findByUserId(userId);
        if (opt.isEmpty()) {
            return null;
        }

        User user = opt.get();

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }

        return user;
    }
}
