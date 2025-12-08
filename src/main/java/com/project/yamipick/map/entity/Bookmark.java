package com.project.yamipick.map.entity;

import com.project.yamipick.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblBookmark", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kakaoPlaceId", "SEQUSER"}) 
})
public class Bookmark {

    @Id
    // ★ 형님! 여기를 수정했습니다. (IDENTITY -> SEQUENCE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqBookmarkGen")
    @SequenceGenerator(
            name = "seqBookmarkGen",
            sequenceName = "seqBookmark_seq", // 아까 만든 DB 시퀀스 이름
            allocationSize = 1
    )
    private Long seqBookmark;

    @Column(nullable = false, length = 50)
    private String kakaoPlaceId; 

    @Column(nullable = false)
    private String name; 

    private String address; 
    private String category; 
    private String phone;
    
    private String x; 
    private String y;
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEQUSER", nullable = false)
    private User user; 
}