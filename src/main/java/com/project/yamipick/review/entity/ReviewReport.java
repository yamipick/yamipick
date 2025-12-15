package com.project.yamipick.review.entity;

import java.sql.Date;

import com.project.yamipick.review.dto.ReviewReportDTO;
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
@Table(name = "tblReviewReport")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReport {
	
	@Id
	@SequenceGenerator(name = "seqReviewReport", allocationSize = 1, sequenceName = "seqReviewReport")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqReviewReport")
	private Long seqReviewReport;
	
	@ManyToOne
    @JoinColumn(name = "seqReview", nullable = false)
	private BoardReview review;
	
	@ManyToOne
    @JoinColumn(name = "seqUser", nullable = false)
    private User user;
	
	@Column(nullable = false, length = 100)
	private String reportReason;
	
	public ReviewReportDTO toDTO() {
		
		return ReviewReportDTO.builder()
							.seqReview(this.review.getSeqReview())
							.seqUser(this.user.getSeqUser())
							.build();
		
	}

}
