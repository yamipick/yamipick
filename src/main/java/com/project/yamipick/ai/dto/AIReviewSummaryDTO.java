package com.project.yamipick.ai.dto;

import com.project.yamipick.ai.entity.AIReviewSummary;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIReviewSummaryDTO {

    private Long seqSummary;
    private String restaurantId;
    private String summaryPublic;
    private String summaryMember;
    private LocalDateTime summaryCreatedAt;
    private LocalDateTime summaryUpdatedAt;

    public AIReviewSummary toEntity() {
        return AIReviewSummary.builder()
                .seqSummary(this.seqSummary)
                .restaurantId(this.restaurantId)
                .summaryPublic(this.summaryPublic)
                .summaryMember(this.summaryMember)
                .summaryCreatedAt(this.summaryCreatedAt)
                .summaryUpdatedAt(this.summaryUpdatedAt)
                .build();
    }
}
