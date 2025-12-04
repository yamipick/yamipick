package com.project.yamipick.admin.service;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.log.entity.BusinessLog; // BanLog 대신 이거 사용!
import com.project.yamipick.log.repository.BusinessLogRepository; // 기존 레포지토리 사용
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserQueryRepository;
import com.project.yamipick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserQueryRepository userQueryRepository;
    private final UserRepository userRepository;
    private final BusinessLogRepository businessLogRepository; // ★ 교체됨

    // 회원 목록 조회
    public Page<AdminUserDTO> getUserList(String keyword, Pageable pageable) {
        Page<User> userPage = userQueryRepository.searchUsers(keyword, pageable);
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

    // 회원 상태 변경 및 통합 로그(BusinessLog) 기록
    @Transactional
    public void updateUserStatus(Long seqUser, String status, String reason) {
        // 1. 회원(타겟) 찾기
        User targetUser = userRepository.findById(seqUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 상태 변경
        targetUser.changeStatus(status);

        // 3. ★ 통합 로그(tblBusinessLog)에 기록
        String adminId = getCurrentAdminId(); // 처리한 관리자 ID 가져오기
        
        if (reason == null || reason.trim().isEmpty()) {
            reason = "관리자(" + adminId + ")에 의한 상태 변경";
        }

        BusinessLog log = BusinessLog.builder()
                .actionType("CHANGE_STATUS")      // 행동 유형 (상태 변경)
                .targetType("USER")               // 대상 유형 (회원)
                .targetId(targetUser.getUserId()) // 대상 ID (정지당한 사람)
                .userId(adminId)                  // 수행자 ID (관리자)
                .message("상태변경: " + status + " / 사유: " + reason) // 상세 내용
                .ipAddr("0.0.0.0")                // 관리자 IP (필요 시 request에서 추출)
                .build();

        businessLogRepository.save(log);
    }

    // 현재 로그인한 관리자 ID 가져오는 유틸 메서드
    private String getCurrentAdminId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system"; // 로그인 정보 없으면 system으로 기록
        }
    }
}