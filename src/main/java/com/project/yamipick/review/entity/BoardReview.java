package com.project.yamipick.review.entity;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tblBoardReview")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardReview {
	
	@Id
	@SequenceGenerator(name = "seqReview", allocationSize = 1, sequenceName = "seqReview")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqReview")
	private Long seqReviewContent;
	
	@OneToOne
	private Long seqStore;
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@Column(nullable = false)
	private Integer starRating;
	
	@OneToOne
	private Long seqUser;
	
	@Lob
    @Column(nullable = false)
	private String reviewContent;
	
	@Column(length = 300)
	private String attach;
	
	@Column(nullable = false)
	private Date regdate;
	
	@Column(nullable = false)
	private Integer readCount;
	
	@Column(nullable = false)
	private Integer favoriteCount;
	
	@Column(nullable = false, length = 20)
	private String contentState = "일반글";

}
