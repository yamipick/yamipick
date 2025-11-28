package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewSummaryDTO {

	private Long seqSummary;
	private Long seqRestaurant;	//식당시퀀스
	private String summaryText;
	private int reviewCount;	//몇 개 리뷰를 요약했는지
	private LocalDateTime createdAt;
	
}
