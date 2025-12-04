package com.project.yamipick.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRecommendResponse {

    private Long seqMenu;
    private String menuName;
    private String menuImage;
    private List<String> matchedTags; // 겹친 태그들
    private int score; // 겹친 개수
    
    private String menuDescription;
    private String reason;
}
