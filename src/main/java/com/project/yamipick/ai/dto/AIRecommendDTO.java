package com.project.yamipick.ai.dto;

import com.project.yamipick.ai.entity.AIRecommend;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendDTO {

    private Long seqRecommend;
    private Long seqMenu;
    private String userInput;
    private String aiReason;
    private LocalDateTime aiCreatedAt;
    private Long seqSession;

    public AIRecommend toEntity() {
        return AIRecommend.builder()
                .seqRecommend(this.seqRecommend)
                .seqMenu(this.seqMenu)
                .userInput(this.userInput)
                .aiReason(this.aiReason)
                .aiCreatedAt(this.aiCreatedAt != null ? this.aiCreatedAt : LocalDateTime.now())
                .seqSession(this.seqSession)
                .build();
    }
}
