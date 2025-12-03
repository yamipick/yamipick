package com.project.yamipick.waiting.dto;

import java.time.format.DateTimeFormatter;

import com.project.yamipick.waiting.domain.Waiting;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WaitingDTO {
    private Long id;
    private int waitingNumber;
    
    // ★ [추가] 매장 이름 (히스토리에서 필수!)
    private String storeName;
    
    private String memberName;
    private String memberPhone;
    private int teamSize;
    private String status;
    private String regDate; // 보기 좋게 String으로 변경

    public WaitingDTO(Waiting entity) {
        this.id = entity.getId();
        this.waitingNumber = entity.getWaitingNumber();
        this.teamSize = entity.getTeamSize();
        
        // 날짜 포맷팅 (2025-12-03 14:30)
        if (entity.getRegDate() != null) {
            this.regDate = entity.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        
        // ★ 매장 이름 가져오기
        if (entity.getOperation() != null && entity.getOperation().getStore() != null) {
            this.storeName = entity.getOperation().getStore().getName();
        }

        if (entity.getMember() != null) {
            this.memberName = entity.getMember().getName();
            this.memberPhone = entity.getMember().getPhoneNumber();
        }
        
        if (entity.getWaitingStatus() != null) {
            this.status = entity.getWaitingStatus().getStatusName();
        }
    }
}