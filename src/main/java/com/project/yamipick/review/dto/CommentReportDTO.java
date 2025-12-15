package com.project.yamipick.review.dto;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Comment;
import com.project.yamipick.review.entity.CommentReport;
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
public class CommentReportDTO {
	
	private Long seqCommentReport;
	private Long seqComment;
	private Long seqUser;
	private String reportReason;
	
	public CommentReport toEntity(Comment comment, User user) {
		
		return CommentReport.builder()
								.seqCommentReport(this.seqCommentReport)
								.comment(comment)
								.user(user)
								.reportReason(this.reportReason)
								.build();
		
	}

}
