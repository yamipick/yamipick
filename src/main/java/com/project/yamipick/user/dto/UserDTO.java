package com.project.yamipick.user.dto;

import java.time.LocalDate;

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
public class UserDTO {
	
	private Long seqUser;
	private String name;
	private String id;
	private String password;
	private String email;
	private String phone;
	private String role;
	private Integer penaltyScore;
	private String nickname;
	private String statusUser;
	private LocalDate createdAt;

}