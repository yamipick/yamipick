package com.project.yamipick.map.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.map.dto.RestaurantDTO; // 기존 DTO 재활용
import com.project.yamipick.map.entity.Bookmark;
import com.project.yamipick.map.repository.BookmarkRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    // 찜 토글 (있으면 삭제, 없으면 저장)
    public boolean toggleBookmark(Long userSeq, RestaurantDTO dto) {
        
        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음"));

        return bookmarkRepository.findByUserAndKakaoPlaceId(user, dto.getId())
                .map(bookmark -> {
                    bookmarkRepository.delete(bookmark); // 이미 있으면 삭제
                    return false; // 찜 해제됨
                })
                .orElseGet(() -> {
                    Bookmark newBookmark = Bookmark.builder()
                            .kakaoPlaceId(dto.getId())
                            .name(dto.getName())
                            .address(dto.getRoadAddress()) // DTO 필드명 확인 필요
                            .phone(dto.getPhone())
                            .category(dto.getCategory())
                            .x(dto.getX())
                            .y(dto.getY())
                            .url(dto.getUrl())
                            .user(user)
                            .build();
                    bookmarkRepository.save(newBookmark); // 없으면 저장
                    return true; // 찜 설정됨
                });
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long userSeq, String kakaoPlaceId) {
        User user = userRepository.findById(userSeq).orElse(null);
        if (user == null) return false;
        return bookmarkRepository.findByUserAndKakaoPlaceId(user, kakaoPlaceId).isPresent();
    }
}