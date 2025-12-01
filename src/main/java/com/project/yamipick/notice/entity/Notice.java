package com.project.yamipick.notice.entity;

import com.project.yamipick.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;

@Entity
@Table(name = "tblNotice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seqNotice")
    private Long seqNotice;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "filePath", length = 500)
    private String filePath;

    @Column(name = "viewCount")
    @ColumnDefault("0")
    private Long viewCount;

    // ★ User가 있어야 에러 안 남
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writerSeq", nullable = false)
    private User writer;

    @CreationTimestamp
    @Column(name = "createdAt", updatable = false)
    private LocalDate createdAt;
}