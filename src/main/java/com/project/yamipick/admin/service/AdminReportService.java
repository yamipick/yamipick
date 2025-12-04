package com.project.yamipick.admin.service;

import com.project.yamipick.admin.dto.AdminReportDTO;
import com.project.yamipick.log.entity.BusinessLog;
import com.project.yamipick.log.repository.BusinessLogRepository;
import com.project.yamipick.report.entity.Report;
import com.project.yamipick.report.repository.ReportQueryRepository;
import com.project.yamipick.report.repository.ReportRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository; // 유저 정보 조회를 위해 필요
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportQueryRepository reportQueryRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository; // ★ 유저 제재를 위해 추가
    private final BusinessLogRepository businessLogRepository; // 로그 저장을 위해 추가

    // 목록 조회
    public Page<AdminReportDTO> getReportList(String status, Pageable pageable) {
        Page<Report> reportPage = reportQueryRepository.searchReports(status, pageable);

        return reportPage.map(report -> AdminReportDTO.builder()
                .seqReport(report.getSeqReport())
                .reporterId(report.getReporter().getUserId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build());
    }

    // ★ [업데이트] 신고 처리 및 회원 제재 로직
    @Transactional
    public void processReport(Long seqReport, String comment, String penaltyType, int penaltyDuration) {
        
        // 1. 신고 내역 조회
        Report report = reportRepository.findById(seqReport)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));

        if ("PROCESSED".equals(report.getStatus())) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        // 2. 신고 상태 완료 처리 (DB Update)
        report.completeProcess(comment);

        // 3. 회원 제재 로직 (penaltyType에 따라 분기)
        if (!"NONE".equals(penaltyType)) {
            // 신고 대상이 '회원' 관련(리뷰, 댓글 등)일 경우, 해당 작성자를 찾아서 제재해야 함
            // 하지만 현재 Report 테이블에는 작성자(target_user_seq) 정보가 명시적으로 없을 수 있음.
            // 일단 targetId가 회원 ID이거나, 별도로 조회 로직이 필요하지만
            // 여기서는 ★신고 대상자(User)를 찾는 로직이 필요함★
            // (가정: 지금은 신고된 게시물의 작성자를 찾기 복잡하니, 임시로 '신고자'를 제재하지 않도록 주의 필요)
            
            // [중요] 실제 구현 시에는 targetId(예: 리뷰ID)를 통해 리뷰 작성자를 찾아오는 로직이 선행되어야 함.
            // 지금은 편의상 "신고 테이블에 targetUserSeq 컬럼이 있다고 가정"하거나
            // "targetType이 USER인 경우 targetId가 회원ID"라고 가정하고 구현합니다.
            
            if ("USER".equals(report.getTargetType())) {
                User targetUser = userRepository.findByUserId(report.getTargetId())
                        .orElseThrow(() -> new IllegalArgumentException("제재 대상 회원을 찾을 수 없습니다."));
                
                String newStatus = "ACTIVE"; // 기본값
                String logMessage = "";

                if ("BLIND_USER".equals(penaltyType)) {
                    newStatus = "BLIND"; // 블라인드(정지)
                    logMessage = "신고 처리에 의한 계정 블라인드 (" + penaltyDuration + "일)";
                    // 기간 저장 로직은 User 엔티티에 '정지 만료일' 컬럼이 있어야 가능함 (지금은 생략)
                } else if ("WITHDRAW_USER".equals(penaltyType)) {
                    newStatus = "WITHDRAWN"; // 강제 탈퇴
                    logMessage = "신고 처리에 의한 강제 탈퇴";
                }

                // 회원 상태 변경
                targetUser.changeStatus(newStatus);
                
                // 제재 로그 남기기
                saveAdminLog(targetUser.getUserId(), "CHANGE_STATUS", logMessage);
            }
        }
    }

    // 로그 저장 유틸 메서드
    private void saveAdminLog(String targetUserId, String actionType, String message) {
        String adminId = "system";
        try {
            adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch(Exception e) {}

        BusinessLog log = BusinessLog.builder()
                .actionType(actionType)
                .targetType("USER")
                .targetId(targetUserId)
                .userId(adminId)
                .message(message)
                .ipAddr("0.0.0.0")
                .build();
        
        businessLogRepository.save(log);
    }
}