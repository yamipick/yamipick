package com.project.yamipick.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReportDTO {
    private Long seqReport;
    private String reporterId;    // 신고자 ID
    private String targetType;    // 대상 유형 (REVIEW, REPLY 등)
    private String targetId;      // 대상 식별자
    private String reason;        // 신고 사유
    private String status;        // 처리 상태 (PENDING, PROCESSED)
    private LocalDate createdAt;  // 신고일
}