package com.project.yamipick.review.dto;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.review.entity.BoardReview;
//import com.project.yamipick.store.entity.Store;
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
public class BoardReviewDTO {
	
	private Long seqReview;
	private Long seqStore;
	private String title;
	private Integer starRating;
	private Long seqUser;
	private String reviewContent;
	private String attach;
	
	private MultipartFile file;
	
	private String place;
	private Timestamp regdate;
	private Integer readCount;
	private Integer favoriteCount;
	private String contentState; // 일반글 / 비밀글
	private String state; // ACTIVE / DELETED
	
	private String nickname;
	private String tags;
	private List<String> tagList;
	private String existingAttach;
	private String displayDate;
	private String userId;
	
	public BoardReview toEntity(User user) {
		
		return BoardReview.builder()
							.seqReview(this.seqReview)
							//.store(store)
							.title(this.title)
							.starRating(this.starRating)
							.user(user)
							.reviewContent(this.reviewContent)
							.attach(this.attach)
							.place(this.place)
							.regdate(this.regdate)
							.readCount(this.readCount)
							.favoriteCount(this.favoriteCount)
							.contentState(this.contentState)
							.build();
	}

}
