package com.project.yamipick.admin.service;

import com.project.yamipick.admin.dto.AdminUserDTO;
import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.repository.BusinessLogRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserQueryRepository;
import com.project.yamipick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserQueryRepository userQueryRepository;
    private final UserRepository userRepository;
    private final BusinessLogRepository businessLogRepository;

    // 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getUserList(String keyword, Pageable pageable) {
        return userQueryRepository.searchUsers(keyword, pageable)
                .map(user -> AdminUserDTO.builder()
                        .seqUser(user.getSeqUser())
                        .userId(user.getUserId())
                        .name(user.getName())
                        .nickname(user.getNickname())
                        .statusUser(user.getStatusUser())
                        .createdAt(user.getCreatedAt()) 
                        .suspendedUntil(user.getSuspendedUntil() != null ? user.getSuspendedUntil().toLocalDate() : null)
                        
                        .build());
    }

    // 상태 변경 로직 (기존 유지)
    public void updateUserStatus(Long seqUser, String status, int durationDays, String reason) {
        User targetUser = userRepository.findById(seqUser)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        LocalDateTime suspendUntil = null;

        if ("SUSPENDED".equals(status)) {
            if (durationDays >= 999) {
                suspendUntil = LocalDateTime.now().plusYears(100);
            } else if (durationDays > 0) {
                suspendUntil = LocalDateTime.now().plusDays(durationDays);
            } else {
                suspendUntil = LocalDateTime.now().plusDays(30);
            }
        }

        targetUser.changeStatus(status, suspendUntil);
        saveAdminLog(targetUser.getUserId(), status, reason);
    }

    private void saveAdminLog(String targetId, String status, String reason) {
        String adminId = "system";
        try { adminId = SecurityContextHolder.getContext().getAuthentication().getName(); } catch(Exception e){}
        
        businessLogRepository.save(BusinessLog.builder()
                .actionType("CHANGE_STATUS")
                .targetType("USER")
                .targetId(targetId)
                .userId(adminId)
                .message("상태: " + status + " / 사유: " + reason)
                .ipAddr("0.0.0.0")
                .build());
    }
}