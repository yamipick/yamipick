package com.project.yamipick.admin.service;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserQueryRepository userQueryRepository;

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
}