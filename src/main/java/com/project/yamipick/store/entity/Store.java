package com.project.yamipick.store.entity;

import com.project.yamipick.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblStore")
public class Store {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqStoreGen")
    @SequenceGenerator(
            name = "seqStoreGen",
            sequenceName = "seqStore",
            allocationSize = 1
    )
    private Long seqStore;
	
	@Column(nullable = false, length = 50)
	private String kakaoPlaceId;
	
	@Column(nullable = false, length = 30)
	private String name;
	
	@Column(nullable = false, length = 300)
	private String address;
	
	@Column(length = 30)
	private String phone;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SEQUSER", nullable = false, unique = true)
	private User user;
	
}
