package com.project.yamipick.map.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NaverReviewDTO {
    private String title;       // 블로그 글 제목
    private String link;        // 블로그 링크
    private String description; // 내용 요약
    private String bloggername; // 블로거 이름
    private String postdate;    // 작성일
}