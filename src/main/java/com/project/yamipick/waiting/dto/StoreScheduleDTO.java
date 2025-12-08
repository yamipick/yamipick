package com.project.yamipick.waiting.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class StoreScheduleDTO {
    private Long storeId;
    private List<Integer> days; // 선택한 요일들 (0:일 ~ 6:토)
    private int dayOfWeek;
    private String openTime;
    private String closeTime;
    private String breakStart;
    private String breakEnd;
    private String isOpen; // "Y" or "N"
}