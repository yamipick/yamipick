package com.project.yamipick.review.dto;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Hashtag;
import com.project.yamipick.review.entity.Tagging;

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
public class TaggingDTO {
	
	private Long seqTagging;
	private Long seqHashtag;
	private Long seqReview;
	
	public Tagging toEntity(Hashtag hashtag, BoardReview review) {
		
		return Tagging.builder()
						.seqTagging(this.seqTagging)
						.hashtag(hashtag)
						.review(review)
						.build();
		
	}

}
