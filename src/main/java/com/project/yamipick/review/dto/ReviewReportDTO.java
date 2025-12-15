package com.project.yamipick.review.dto;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.ReviewReport;
import com.project.yamipick.user.entity.User;

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
public class ReviewReportDTO {
	
	private Long seqReviewReport;
	private Long seqReview;
	private Long seqUser;
	private String reportReason;
	
	public ReviewReport toEntity(BoardReview review, User user) {
		
		return ReviewReport.builder()
							.review(review)
							.user(user)
							.build();
		
	}

}
