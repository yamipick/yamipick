package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import com.project.yamipick.ai.entity.AIReviewSummary;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewSummaryResponse {

	private String restaurantId;
	private String summaryPublic;
	private String summaryMember;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public static ReviewSummaryResponse of(AIReviewSummary e) {
		return ReviewSummaryResponse.builder()
				.restaurantId(e.getRestaurantId())
				.summaryPublic(e.getSummaryPublic())
				.summaryMember(e.getSummaryMember())
				.createdAt(e.getSummaryCreatedAt())
				.updatedAt(e.getSummaryUpdatedAt())
				.build();
	}
	
}
