package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewSummaryDTO {

	private Long seqReviewSummary;  // PK
    private Long seqUser;           // 회원용 요약이면 FK, 비회원이면 null
    private Long placeId;           // 리뷰 대상 장소 / 식당 ID (FK)
    private String summaryText;     // AI가 생성한 최종 요약 결과
    private Integer reviewCount;    // 요약에 포함된 리뷰 개수
    private String sentiment;       // 긍/부정 요약 (optional)
    private LocalDateTime createdAt;
    
}
