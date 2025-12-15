package com.project.yamipick.review.entity;

import java.sql.Timestamp;

import com.project.yamipick.review.dto.ScrapReviewDTO;
import com.project.yamipick.user.entity.User;

import jakarta.persistence.Column;
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
@Table(name = "tblScrapReview")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapReview {
	
	@Id
	@SequenceGenerator(name = "seqscrapReview", allocationSize = 1, sequenceName = "seqscrapReview")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqscrapReview")
	private Long seqscrapReview;
	
	@ManyToOne
	@JoinColumn(name = "seqReview", nullable = false)
	private BoardReview review;
	
	@ManyToOne
    @JoinColumn(name = "seqUser", nullable = false)
    private User user;
	
	@Column(nullable = false)
	private Timestamp regdate;
	
	public ScrapReviewDTO toDTO() {
		
		return ScrapReviewDTO.builder()
							.seqscrapReview(this.seqscrapReview)
							.seqReview(this.review.getSeqReview())
							.seqUser(this.user.getSeqUser())
							.regdate(this.regdate)
							.build();
		
	}

}
