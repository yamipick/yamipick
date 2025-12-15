package com.project.yamipick.reservation.dto;

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
public class StoreTableTypeDTO {

	private Long seqStoreTable;
	private String name;
	private Integer capacity;
	private Integer quantity;
	
	private Long seqStore;
	
}
