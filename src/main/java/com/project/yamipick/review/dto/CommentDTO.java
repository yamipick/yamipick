package com.project.yamipick.review.dto;

import java.sql.Date;

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
	private Date regdate;

}
