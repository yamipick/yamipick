package com.project.yamipick.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRecommendRequest {

	private List<String> positiveTags;  // ExtractedTags에서 복사됨
    private List<String> negativeTags;
	
    private String userInput;        // 챗봇 입력 문장
    private Long seqSession;         // 챗봇 세션 (태그 추천은 null)
    private String emotion;
    private List<Long> excludeMenuIds;
}

