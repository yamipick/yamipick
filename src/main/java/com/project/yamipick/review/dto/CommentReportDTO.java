package com.project.yamipick.review.dto;

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

}
