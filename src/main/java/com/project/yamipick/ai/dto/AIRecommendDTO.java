package com.project.yamipick.ai.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AIRecommendDTO {
	
	private Long seqAIRecommend; // PK
    private Long seqUser;        // 회원일 경우 저장, 비회원은 null
    private String category;     // 음식 카테고리 (한식/중식/일식 등)
    private String menuName;     // 추천 메뉴명
    private String storeName;    // 추천 식당명
    private String address;      // 식당 주소
    private String priceRange;   // 가격대
    private String imageUrl;     // 메뉴/식당 이미지 URL
    private String reason;       // 추천 이유 (GPT가 제공)
    private String source;       // 추천 방식 (GPT, TOURAPI 등)
    private LocalDateTime createdAt;
    
}
