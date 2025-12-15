package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import com.project.yamipick.ai.entity.AIRecommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendDTO {

	private Long seqRecommend;    // PK
    private Long seqMenu;         // 추천된 메뉴 번호
    private String userInput;     // 사용자가 입력한 문장
    private String aiReason;      // 추천 이유
    private LocalDateTime aiCreatedAt; // 생성 시간
    private Long seqSession;      // AI 세션 번호(FK)
	
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
