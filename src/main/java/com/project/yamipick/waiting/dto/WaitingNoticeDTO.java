package com.project.yamipick.waiting.dto;

import java.time.format.DateTimeFormatter;

import com.project.yamipick.waiting.domain.WaitingNotice;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WaitingNoticeDTO {
    private Long id;
    private String title;
    private String content;
    private boolean isPinned;
    private String regDate;

    public WaitingNoticeDTO(WaitingNotice entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.isPinned = "Y".equals(entity.getIsPinned());
        // 날짜를 보기 좋게 변환 (예: 2023-12-03)
        if(entity.getRegDate() != null) {
            this.regDate = entity.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}
