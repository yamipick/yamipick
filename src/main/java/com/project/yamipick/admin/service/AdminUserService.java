package com.project.yamipick.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserQueryRepository;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserQueryRepository userQueryRepository;
    private final UserRepository userRepository;

    public Page<AdminUserDTO> getUserList(String keyword, Pageable pageable) {
        
        // 1. 레포지토리에서 Entity 가져오기
        Page<User> userPage = userQueryRepository.searchUsers(keyword, pageable);

        // 2. Entity -> DTO 변환 (화면에 뿌리기 위해)
        return userPage.map(user -> AdminUserDTO.builder()
                .seqUser(user.getSeqUser())
                .userId(user.getUserId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .statusUser(user.getStatusUser())
                .penaltyScore(user.getPenaltyScore())
                .createdAt(user.getCreatedAt())
                .build());
    }
    
    // 회원 상태 변경 (정지/해제)
    @Transactional
    public void updateUserStatus(Long seqUser, String status, String reason) {
        // 1. 회원 찾기
        User user = userRepository.findById(seqUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 상태 변경 (JPA 변경 감지 작동)
        user.changeStatus(status);

        // 3. (나중에 구현) 여기서 'BanLog' 테이블에 reason(사유)을 insert 하면 C-04 요구사항 100% 충족
        // log.info("회원 상태 변경: ID={}, Status={}, Reason={}", user.getUserId(), status, reason);
    }
    
}