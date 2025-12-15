package com.project.yamipick.review.dto;

import com.project.yamipick.review.entity.Hashtag;

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
public class HashtagDTO {
	
	private Long seqHashtag;
	private String hashtag;
	
	public Hashtag toEntity() {
		
		return Hashtag.builder()
						.seqHashtag(this.seqHashtag)
						.hashtag(this.hashtag)
						.build();
		
	}

}
