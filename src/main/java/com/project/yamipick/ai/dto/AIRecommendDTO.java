package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AIRecommendDTO {
	
	private Long seqAi;					
	private Long seqUser;				
	private String category;			
	private String userInput;			
	private String recommendResult;
	private LocalDateTime createdAt;

}
