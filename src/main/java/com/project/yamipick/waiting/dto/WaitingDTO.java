package com.project.yamipick.waiting.dto;

import java.time.LocalDateTime;

import com.project.yamipick.waiting.domain.Waiting;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WaitingDTO {
    private Long id;
    private String memberName;
    private String memberPhone;
    private int teamSize;
    private String status;
    private LocalDateTime regDate; // JSON 변환 시 "2023-12-02T10:00:00" 형식으로 나감

    public WaitingDTO(Waiting entity) {
        this.id = entity.getId();
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