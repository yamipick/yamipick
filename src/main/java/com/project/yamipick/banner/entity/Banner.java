package com.project.yamipick.banner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "tblBanner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Banner {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqBanner")
    private Long seqBanner;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "imgPath", nullable = false, length = 500)
    private String imgPath;

    @Column(name = "isVisible", length = 1)
    @ColumnDefault("'Y'")
    private String isVisible;
}