package com.project.yamipick.review.entity;

import java.sql.Timestamp;

import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tblComment")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
	
	@Id
	@SequenceGenerator(name = "seqComment", allocationSize = 1, sequenceName = "seqComment")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqComment")
	private Long seqComment;
	
	@ManyToOne
    @JoinColumn(name = "seqReview", nullable = false)
	private BoardReview review;
	
	@ManyToOne
    @JoinColumn(name = "seqParentComment")
    private Comment parentComment;
	
	@ManyToOne
    @JoinColumn(name = "seqUser", nullable = false)
    private User user;
	
	@Lob
    @Column(nullable = false)
    private String content;
	
	@Column(nullable = false)
    private Timestamp regdate;
	
	public CommentDTO toDTO() {
		
		return CommentDTO.builder()
						.seqComment(this.seqComment)
						.seqReview(this.review.getSeqReview())
						.seqParentComment(this.parentComment != null ? this.parentComment.getSeqComment() : null)
						.seqUser(this.user.getSeqUser())
						.content(this.content)
						.regdate(this.regdate)
						.nickname(this.user.getNickname())
						.build();
		
	}

}
