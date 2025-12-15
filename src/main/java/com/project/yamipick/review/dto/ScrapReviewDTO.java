package com.project.yamipick.review.dto;

import java.sql.Timestamp;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.ScrapReview;
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
public class ScrapReviewDTO {
	
	private Long seqscrapReview;
	private Long seqReview;
	private Long seqUser;
	private Timestamp regdate;

	public ScrapReview toEntity(BoardReview review, User user) {
		
		return ScrapReview.builder()
							.seqscrapReview(this.seqscrapReview)
							.review(review)
							.user(user)
							.regdate(this.regdate)
							.build();
		
	}

}
