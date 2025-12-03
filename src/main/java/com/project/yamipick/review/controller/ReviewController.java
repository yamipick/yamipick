package com.project.yamipick.review.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {
	
	@GetMapping("/review/reviewmain")
	public String reviewmain() {
		
		return "review/reviewmain";
	}
	
	@GetMapping("/review/reviewlist")
	public String reviewlist() {
		
		return "review/reviewlist";
	}
	
	@GetMapping("/review/reviewview")
	public String reviewview() {
		
		return "review/reviewview";
	}
	
	@GetMapping("/review/reviewadd")
	public String reviewadd() {
		
		return "review/reviewadd";
	}
	
	@PostMapping("/review/reviewaddok")
	public String reviewaddok() {
		
		return "review/reviewaddok";
	}
	
	@GetMapping("/review/reviewedit")
	public String reviewedit() {
		
		return "review/reviewedit";
	}
	
	@PostMapping("/review/revieweditok")
	public String revieweditok() {
		
		return "review/revieweditok";
	}
	
	@PostMapping("/review/reviewdeleteok")
	public String reviewdeleteok() {
		
		return "review/reviewdeleteok";
	}

}
