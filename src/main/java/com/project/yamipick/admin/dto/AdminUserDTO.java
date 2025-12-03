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
public class AdminUserDTO {
    private Long seqUser;
    private String userId;      // 아이디
    private String name;        // 이름
    private String nickname;    // 닉네임
    private String email;
    private String phone;
    private String statusUser;  // 상태 (ACTIVE, SUSPENDED 등)
    private Integer penaltyScore; // 벌점
    private LocalDate createdAt; // 가입일
}