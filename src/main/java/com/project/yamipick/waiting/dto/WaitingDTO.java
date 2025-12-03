package com.project.yamipick.waiting.dto;

import java.time.LocalDateTime;

import com.project.yamipick.waiting.domain.Waiting;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WaitingDTO {
    private Long id;              // 시스템 PK
    private int waitingNumber;    // ★ 보여줄 대기번호 (1번, 2번...)
    private String memberName;
    private String memberPhone;
    private int teamSize;
    private String status;
    private LocalDateTime regDate;

    public WaitingDTO(Waiting entity) {
        this.id = entity.getId();
        this.waitingNumber = entity.getWaitingNumber(); // 추가됨
        this.teamSize = entity.getTeamSize();
        this.regDate = entity.getRegDate();
        
        if (entity.getMember() != null) {
            this.memberName = entity.getMember().getName();
            this.memberPhone = entity.getMember().getPhoneNumber();
        }
        
        if (entity.getWaitingStatus() != null) {
            this.status = entity.getWaitingStatus().getStatusName();
        }
    }
}