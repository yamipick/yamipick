package com.project.yamipick.banner.repository;

import com.project.yamipick.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    // 노출(Y) 설정된 배너만, 순서대로 가져오기
    List<Banner> findAllByIsVisibleOrderBySortOrderAsc(String isVisible);
}