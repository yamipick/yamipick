package com.project.yamipick.store.dto;

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
public class StoreDTO {
	
	private Long seqStore;
	private String kakaoPlaceId;
	private String name;
	private String address;
	private String phone;

}
