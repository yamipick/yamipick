package com.project.yamipick.log.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {
    private String actionType;  // 예: CLICK, VIEW, LOGIN, ERROR
    private String targetType;  // 예: STORE, MAP, SYSTEM
    private String targetId;    // 예: store_101, admin
    private String userId;      // 누가? (비회원이면 IP나 세션ID)
    private String message;     // 상세 내용
    private String ipAddr;      // 접속 IP
    private LocalDateTime createdAt;
}