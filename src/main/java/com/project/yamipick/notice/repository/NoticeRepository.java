package com.project.yamipick.notice.repository;

import com.project.yamipick.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    // 최신순으로 전체 조회 (SELECT * FROM tblNotice ORDER BY createdAt DESC)
    List<Notice> findAllByOrderByCreatedAtDesc();
}