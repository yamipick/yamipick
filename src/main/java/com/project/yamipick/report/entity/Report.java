package com.project.yamipick.report.entity;

import com.project.yamipick.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;

@Entity
@Table(name = "tblReport")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqReport")
    private Long seqReport;

    // ★ User가 있어야 에러 안 남
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seqUser", nullable = false)
    private User reporter;

    @Column(name = "targetType", nullable = false, length = 20)
    private String targetType;

    @Column(name = "targetId", nullable = false, length = 50)
    private String targetId;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false, length = 20)
    @ColumnDefault("'PENDING'")
    private String status;

    @Column(name = "adminComment", length = 1000)
    private String adminComment;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDate createdAt;
    
    /**
     * [비즈니스 로직] 신고 처리 완료
     * @param comment 관리자 처리 내용 (예: "욕설 확인되어 블라인드 처리함")
     */
    public void completeProcess(String comment) {
        this.status = "PROCESSED";
        this.adminComment = comment;
    }
}