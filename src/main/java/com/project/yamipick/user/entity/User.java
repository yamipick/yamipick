package com.project.yamipick.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "tblUser") 
@Getter 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqUser")
    private Long seqUser;
    
    // ... 나머지 필드는 나중에 팀원이 채워도 됨 (일단 seqUser만 있으면 연결 가능)
}
