package com.project.yamipick.waiting.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class NoticeRequestDTO {
    private Long id;        // 수정/삭제 시 필요
    private Long storeId;   // 등록 시 필요
    private String title;
    private String content;
    private Boolean isPinned; // null 방지를 위해 Boolean 객체 사용
}
