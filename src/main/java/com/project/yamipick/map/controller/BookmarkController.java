package com.project.yamipick.map.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.project.yamipick.map.dto.RestaurantDTO;
import com.project.yamipick.map.service.BookmarkService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookmark") // URL 경로 분리
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 찜하기 버튼 클릭 (POST)
    @PostMapping("/toggle")
    @ResponseBody
    public ResponseEntity<Boolean> toggle(@RequestBody RestaurantDTO dto) {
        // ★ 세션 연동 전 임시 하드코딩 (1번 유저)
        Long userSeq = 1L; 
        
        boolean result = bookmarkService.toggleBookmark(userSeq, dto);
        return ResponseEntity.ok(result);
    }

    // 상태 확인 (GET)
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Boolean> status(@RequestParam("id") String kakaoPlaceId) {
        // ★ 세션 연동 전 임시 하드코딩 (1번 유저)
        Long userSeq = 1L; 
        
        return ResponseEntity.ok(bookmarkService.isBookmarked(userSeq, kakaoPlaceId));
    }
}