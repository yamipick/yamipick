package com.project.yamipick.user.entity;

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
import lombok.ToString;

@Entity
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblUser")
public class User {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqUserGen")
    @SequenceGenerator(
            name = "seqUserGen",
            sequenceName = "seqUser",
            allocationSize = 1
    )
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
