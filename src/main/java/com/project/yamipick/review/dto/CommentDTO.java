package com.project.yamipick.review.dto;

import java.sql.Timestamp;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Comment;
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
public class CommentDTO {
	
	private Long seqComment;
	private Long seqReview;
	private Long seqParentComment;
	private Long seqUser;
	private String content;
	private Timestamp regdate;
	private String state;
	
	private String nickname;
	private int depth = 0;
	
	public Comment toEntity(BoardReview review, User user, Comment parent) {
		
		return Comment.builder()
						.seqComment(this.seqComment)
						.review(review)
						.parentComment(parent)
						.user(user)
						.content(this.content)
						.regdate(this.regdate)
						.build();
		
	}

}
