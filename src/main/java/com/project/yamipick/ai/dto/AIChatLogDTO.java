package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AIChatLogDTO {

	private Long seqChat;
	private Long seqUser;
	private String userMessage;
	private String aiResponse;
	private LocalDateTime createdAt;
	
}
