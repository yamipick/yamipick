package com.project.yamipick.review.entity;

import java.sql.Date;

import com.project.yamipick.review.dto.HashtagDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tblHashtag")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hashtag {
	
	@Id
	@SequenceGenerator(name = "seqHashtag", allocationSize = 1, sequenceName = "seqHashtag")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqHashtag")
	private Long seqHashtag;
	
	@Column(nullable = false, length = 100)
	private String hashtag;
	
	public HashtagDTO toDTO() {
		
		return HashtagDTO.builder()
						.seqHashtag(this.seqHashtag)
						.hashtag(this.hashtag)
						.build();
		
	}

}
