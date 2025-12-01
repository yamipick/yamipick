package com.project.yamipick.review.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardReviewDTO {
	
	private Long seqReview;
	private Long seqStore;
	private String title;
	private Integer starRating;
	private Long seqUser;
	private String reviewContent;
	private String attach;
	private Date regdate;
	private Integer readCount;
	private Integer favoriteCount;
	private String contentState;

}
