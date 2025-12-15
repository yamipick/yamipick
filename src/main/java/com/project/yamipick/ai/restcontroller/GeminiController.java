package com.project.yamipick.ai.restcontroller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.service.AIService;
import com.project.yamipick.ai.service.GeminiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GeminiController {

	private final AIService aiService;
	private final GeminiService geminiService;
	
	@PostMapping("/api/gemini/menu-test")
	public String createGeminiMenuTest(@RequestBody PromptRequest req) {
	    return geminiService.call(req.getPrompt()).blockOptional().orElse("");
	}

	// 내부 요청 DTO
	private static class PromptRequest {
		private String prompt;
		public String getPrompt() { return prompt; }
		public void setPrompt(String prompt) { this.prompt = prompt; }
	}
}

