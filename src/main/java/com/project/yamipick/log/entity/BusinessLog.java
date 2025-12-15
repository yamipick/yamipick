package com.project.yamipick.log.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
//인덱스 추가(10만건 읽는데 딜레이가 3초 정도 걸려서)
@Table(name = "tblBusinessLog", indexes = {
 @Index(name = "idx_log_created_at", columnList = "createdAt"),  // 날짜 검색용
 @Index(name = "idx_log_action_type", columnList = "actionType") // 행동 검색용
})
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