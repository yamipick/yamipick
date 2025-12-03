package com.project.yamipick.review.entity;

import com.project.yamipick.review.dto.TaggingDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tblTagging")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tagging {
	
	@Id
	@SequenceGenerator(name = "seqTagging", allocationSize = 1, sequenceName = "seqTagging")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqTagging")
	private Long seqTagging;
	
	@ManyToOne
	@JoinColumn(name = "seqHashtag", nullable = false)
	private Hashtag hashtag;
	
	@ManyToOne
	@JoinColumn(name = "seqReview", nullable = false)
	private BoardReview review;
	
	public TaggingDTO toDTO() {
		
		return TaggingDTO.builder()
						.seqTagging(this.seqTagging)
						.seqHashtag(this.hashtag.getSeqHashtag())
						.seqReview(this.review.getSeqReview())
						.build();
		
	}

}
