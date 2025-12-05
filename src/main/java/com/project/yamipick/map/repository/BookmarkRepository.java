package com.project.yamipick.map.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.yamipick.map.entity.Bookmark;
import com.project.yamipick.user.entity.User;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    // "이 유저가 + 이 가게를" 찜했는지 확인
    Optional<Bookmark> findByUserAndKakaoPlaceId(User user, String kakaoPlaceId);
}