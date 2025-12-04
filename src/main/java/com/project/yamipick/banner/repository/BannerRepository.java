package com.project.yamipick.banner.repository;

import com.project.yamipick.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    // 관리자용: 전체 조회 (최신 등록순)
    List<Banner> findAllByOrderBySeqBannerDesc();

    // 사용자용: 노출('Y')된 배너만 조회 (최신 등록순)
    List<Banner> findByIsVisibleOrderBySeqBannerDesc(String isVisible);
}