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
    private String storeName;       // 매장 이름
    private String memberName;      // 회원 이름
    private String memberPhone;     // 회원 전화번호
    private int teamSize;
    private String status;
    private String regDate;         // 등록일 (포맷팅된 문자열)

    public WaitingDTO(Waiting entity) {
        this.id = entity.getId();
        this.waitingNumber = entity.getWaitingNumber();
        this.teamSize = entity.getTeamSize();

        // 날짜 포맷팅 (2025-12-03 14:30)
        if (entity.getRegDate() != null) {
            this.regDate = entity.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        // 매장 이름 가져오기
        if (entity.getOperation() != null && entity.getOperation().getStore() != null) {
            this.storeName = entity.getOperation().getStore().getName();
        }

        // ✅ 변경: entity.getMember() → entity.getUser()
        if (entity.getUser() != null) {
            this.memberName = entity.getUser().getName();
            this.memberPhone = entity.getUser().getPhone();  // ✅ getPhoneNumber() → getPhone()
        }

        // 상태 정보
        if (entity.getWaitingStatus() != null) {
            this.status = entity.getWaitingStatus().getStatusName();
        }
    }
}