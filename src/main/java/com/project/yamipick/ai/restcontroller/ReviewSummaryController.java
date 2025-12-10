package com.project.yamipick.ai.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.service.ReviewSummaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewSummaryController {

    private final ReviewSummaryService summaryService;

    /** 리뷰 요약 조회 및 생성 */
    @GetMapping("/{restaurantId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable String restaurantId) {
        return ResponseEntity.ok(summaryService.summarize(restaurantId));
    }
}
