package com.project.yamipick.log.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "BUSINESS_LOG") // ★ 테이블 이름 변경
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BusinessLog { // ★ 클래스 이름 변경

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqLog")
    private Long seqLog;

    @Column(name = "actionType", length = 1000)
    private String actionType;

    @Column(name = "targetType", length = 1000)
    private String targetType;

    @Column(name = "targetId", length = 1000)
    private String targetId;

    @Column(name = "userId", length = 1000)
    private String userId;

    @Lob
    @Column(name = "message")
    private String message;

    @Column(name = "ipAddr", length = 500)
    private String ipAddr;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDateTime createdAt;
}