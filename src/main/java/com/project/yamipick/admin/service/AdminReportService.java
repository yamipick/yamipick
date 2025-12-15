package com.project.yamipick.admin.service;

import com.project.yamipick.admin.dto.AdminReportDTO;
import com.project.yamipick.report.entity.Report;
import com.project.yamipick.report.repository.ReportQueryRepository;
import com.project.yamipick.report.repository.ReportRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReportService {

    private final ReportQueryRepository reportQueryRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // ★ ReviewRepository, ReplyRepository는 아직 없으므로 제거! (충돌 방지)

    // 1. 목록 조회 (기존 유지)
    @Transactional(readOnly = true)
    public Page<AdminReportDTO> getReportList(String status, Pageable pageable) {
        return reportQueryRepository.searchReports(status, pageable)
                .map(report -> AdminReportDTO.builder()
                        .seqReport(report.getSeqReport())
                        .reporterId(report.getReporter().getUserId())
                        .targetType(report.getTargetType())
                        .targetId(report.getTargetId())
                        .reason(report.getReason())
                        .status(report.getStatus())
                        .createdAt(report.getCreatedAt())
                        .build());
    }

    /**
     * 2. [안전한 자동화 버전]
     * - USER 신고: 자동 정지 OK
     * - REVIEW 신고: 작성자를 못 찾으므로 상태만 변경 (나중에 합치면 기능 추가)
     */
    public void processReport(Long seqReport, String comment, String penaltyType, int penaltyDuration) {
        
        Report report = reportRepository.findById(seqReport)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역이 없습니다."));

        // (1) 상태 변경
        report.setStatus("PROCESSED");

        String finalComment = comment;
        if (!"NONE".equals(penaltyType)) {
            finalComment += " [조치: " + penaltyType + " / " + penaltyDuration + "일]";
        }
        
        // (2) 작성자(정지 대상) 찾기 로직
        User targetUser = null;
        
        // ID에서 숫자만 추출 ("review_101" -> 101)
        String rawId = report.getTargetId(); 
        String numberOnly = rawId.replaceAll("[^0-9]", ""); 
        Long targetIdSeq = Long.parseLong(numberOnly);

        if ("USER".equals(report.getTargetType())) {
            // ★ 대상이 회원이면 바로 찾을 수 있음 (내 영역이니까!)
            targetUser = userRepository.findById(targetIdSeq).orElse(null);
        } 
        else {
            // ★ 대상이 리뷰나 댓글이면?
            // 아직 ReviewRepository가 없어서 작성자를 못 찾음.
            // 여기서는 targetUser를 null로 둬서, 자동 정지는 건너뛰고 "처리 완료"만 되게 함.
            finalComment += " (작성자 조회 불가로 자동 정지 미적용)";
        }

        // (3) 찾은 유저가 있으면 -> 정지 먹이기 (User 신고일 때만 동작)
        if (targetUser != null && !"NONE".equals(penaltyType)) {
            applyPenaltyToUser(targetUser, penaltyType, penaltyDuration);
            finalComment += " -> 회원 정지 적용됨";
        }
        
        // 최종 저장
        report.setAdminComment(finalComment);
        reportRepository.save(report);
    }

    // 정지 로직 (기존과 동일)
    private void applyPenaltyToUser(User user, String penaltyType, int duration) {
        String newStatus = "ACTIVE";
        LocalDateTime suspendUntil = null;

        switch (penaltyType) {
            case "SUSPEND_3":
            case "SUSPEND_7":
                newStatus = "SUSPENDED";
                suspendUntil = LocalDateTime.now().plusDays(duration);
                break;
            case "SUSPEND_PERMANENT":
                newStatus = "SUSPENDED";
                suspendUntil = LocalDateTime.now().plusYears(100);
                break;
            case "WITHDRAW":
                newStatus = "WITHDRAWN";
                suspendUntil = null;
                break;
        }
        user.changeStatus(newStatus, suspendUntil);
    }
}