package com.project.yamipick.reservation.entity;

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
@Table(name = "tblUser")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
	
	@Id
	@SequenceGenerator(name = "seqUserGen", sequenceName = "seqUser", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqUserGen")
	@Column(name = "seqUser")
	private Long seqUser;
	
	@Column(nullable = false, length = 30)
	private String name;
	
	@Column(nullable = false, length = 30)
	private String id;
	
	@Column(nullable = false, length = 300)
	private String password;
	
	@Column(nullable = false, length = 50)
	private String email;
	
	@Column(nullable = false, length = 30)
	private String phone;
	
	@Column(nullable = false, length = 100)
	private String role;
	
	@Column(nullable = false)
	private Integer penaltyScore;
	
	@Column(nullable = false, length = 100)
	private String nickname;
	
	@Column(nullable = false, length = 50)
	private String statusUser;

}
