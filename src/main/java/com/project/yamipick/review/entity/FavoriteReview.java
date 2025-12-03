package com.project.yamipick.review.entity;

import java.sql.Date;

import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tblFavoriteReview")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteReview {
	
	@Id
	@SequenceGenerator(name = "seqFavoriteReview", allocationSize = 1, sequenceName = "seqFavoriteReview")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqFavoriteReview")
	private Long seqFavoriteReview;
	
	@ManyToOne
	@JoinColumn(name = "seqReview", nullable = false)
	private BoardReview review;
	
	@ManyToOne
    @JoinColumn(name = "seqUser", nullable = false)
    private User user;
	
	@Column(nullable = false)
	private Date regdate;
	
	public FavoriteReviewDTO toDTO() {
		
		return FavoriteReviewDTO.builder()
								.seqFavoriteReview(this.seqFavoriteReview)
								.seqReview(this.review.getSeqReview())
								.seqUser(this.user.getSeqUser())
								.regdate(this.regdate)
								.build();
		
	}

}
