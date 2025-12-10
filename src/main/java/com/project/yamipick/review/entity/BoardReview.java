package com.project.yamipick.review.entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.store.entity.Store;
//import com.project.yamipick.store.entity.Store;
import com.project.yamipick.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tblBoardReview")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardReview {
	
	@Id
	@SequenceGenerator(name = "seqReview", allocationSize = 1, sequenceName = "seqReview")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqReview")
	private Long seqReview;
	
	//@ManyToOne
	//@JoinColumn(name = "seqStore")
	//private Store store;
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@ManyToOne
	@JoinColumn(name = "seqStore")
	private Store store;
	
	private Integer starRating;
	
	@ManyToOne
	@JoinColumn(name = "seqUser", nullable = false)
	private User user;
	
	@Lob
    @Column(nullable = false)
	private String reviewContent;
	
	@Column(length = 300)
	private String attach;
	
	@Column(length = 300)
	private String place;
	
	@Column(nullable = false)
	private Date regdate;
	
	@Column(nullable = false)
	private Integer readCount;
	
	@Column(nullable = false)
	private Integer favoriteCount;
	
	@Column(nullable = false, length = 20)
	private String contentState;
	
	@OneToMany(mappedBy = "review")
	private List<Tagging> taggings = new ArrayList<>();
	
	public BoardReviewDTO toDTO() {
		
		return BoardReviewDTO.builder()
							.seqReview(this.seqReview)
							//.seqStore(this.store.getSeqStore())
							.title(this.title)
							.starRating(this.starRating)
							.seqUser(this.user.getSeqUser())
							.nickname(this.user.getNickname())
							.reviewContent(this.reviewContent)
							.attach(this.attach)
							.place(this.place)
							.regdate(this.regdate)
							.contentState(this.contentState)
							.build();
	}

}
