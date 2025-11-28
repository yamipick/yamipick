package com.project.yamipick.log.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "BUSINESS_LOG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BusinessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqLog") // 카멜케이스 컬럼명
    private Long seqLog;

    @Column(name = "actionType", length = 1000)
    private String actionType;

    @Column(name = "targetType", length = 1000)
    private String targetType;

    @Column(name = "targetId", length = 1000)
    private String targetId;

    @Column(name = "userId", length = 1000)
    private String userId;

    @Lob // CLOB 타입 (대용량 텍스트)
    @Column(name = "message")
    private String message;

    @Column(name = "ipAddr", length = 500)
    private String ipAddr;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;
}