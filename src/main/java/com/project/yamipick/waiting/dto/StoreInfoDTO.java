package com.project.yamipick.waiting.dto;

import com.project.yamipick.waiting.domain.WaitingStore;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StoreInfoDTO {
    // ERD에 있는 실제 데이터
    private Long id;
    private String name;
    private String address;
    private String phone;
    
    // 계산된 데이터 (DB에 없음)
    private boolean waitingOpen; 
    private String todayHours;   

    // 엔티티 -> DTO 변환 생성자
    public StoreInfoDTO(WaitingStore entity, boolean isOpen, String hours) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.address = entity.getAddress();
        this.phone = entity.getPhone();
        this.waitingOpen = isOpen;
        this.todayHours = hours;
    }
}
