package com.project.yamipick.review.entity;

import com.project.yamipick.review.dto.CommentReportDTO;
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
@Table(name = "tblCommentReport")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReport {
	
	@Id
	@SequenceGenerator(name = "seqCommentReport", allocationSize = 1, sequenceName = "seqCommentReport")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqCommentReport")
	private Long seqCommentReport;
	
	@ManyToOne
	@JoinColumn(name = "seqComment")
	private Comment comment;
	
	@ManyToOne
	@JoinColumn(name = "seqUser")
	private User user;
	
	@Column(nullable = false, length = 100)
	private String reportReason;
	
	public CommentReportDTO toDTO() {
		
		return CommentReportDTO.builder()
								.seqCommentReport(this.seqCommentReport)
								.seqComment(this.comment.getSeqComment())
								.seqUser(this.user.getSeqUser())
								.reportReason(this.reportReason)
								.build();
		
	}

}
