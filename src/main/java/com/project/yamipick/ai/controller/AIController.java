package com.project.yamipick.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.dto.AIRecommendDTO;
import com.project.yamipick.ai.dto.ReviewSummaryDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AIController {

	@PostMapping("/recommend")
	public ResponseEntity<?> recommend(@RequestBody AIRecommendDTO dto) {
		
		return ResponseEntity.ok().body("AI recommend API ready");
	}
	
	@PostMapping("/summary")
	public ResponseEntity<?> summary(@RequestBody ReviewSummaryDTO dto) {
		
		return ResponseEntity.ok().body("AI summary API ready");
	}
	
}
