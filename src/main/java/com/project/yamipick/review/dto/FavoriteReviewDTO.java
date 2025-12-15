package com.project.yamipick.review.dto;

import java.sql.Timestamp;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.FavoriteReview;
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
public class FavoriteReviewDTO {
	
	private Long seqFavoriteReview;
	private Long seqReview;
	private Long seqUser;
	private Timestamp regdate;
	
	public FavoriteReview toEntity(BoardReview review, User user) {
		
		return FavoriteReview.builder()
								.seqFavoriteReview(this.seqFavoriteReview)
								.review(review)
								.user(user)
								.regdate(this.regdate)
								.build();
		
	}

}
