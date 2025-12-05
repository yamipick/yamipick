package com.project.yamipick.review.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {
	
	private final ReviewService reviewService;
	
	@GetMapping("/review/reviewmain")
	public String reviewmain() {
		
		return "review/reviewmain";
	}
	
	@GetMapping("/review/reviewlist")
	public String reviewlist() {
		
		return "review/reviewlist";
	}
	
	@GetMapping("/review/activity")
	public String activity(@RequestParam(name="tab", defaultValue = "review") String tab,
	                       Model model, Principal principal) {

		//String seqUser = principal.getName();
	    //Long id = Long.parseLong(seqUser);
		
		//임시 로그인
		Long id;

	    if (principal == null) {
	        // ⭐ 임시 로그인 ID 강제 적용
	        id = 1L;  
	        System.out.println("※ 임시 로그인 적용됨: userId=1");
	    } else {
	        id = Long.parseLong(principal.getName());
	    }
	    

	    switch (tab) {
	        case "review":
	            model.addAttribute("list", reviewService.getMyReviews(id));
	            break;

	        case "comment":
	            model.addAttribute("list", reviewService.getMyComments(id));
	            break;

	        case "favorite":
	            model.addAttribute("list", reviewService.getMyFavorites(id));
	            break;

	        case "scrap":
	            model.addAttribute("list", reviewService.getMyScraps(id));
	            break;
	    }

	    model.addAttribute("tab", tab);

	    return "review/activity"; // 하나의 템플릿만 사용
	}


	
	@GetMapping("/review/reviewview")
	public String reviewview(@RequestParam("seqReview") Long seqReview, Model model) {

	    BoardReviewDTO dto = reviewService.getReview(seqReview); // DTO로 조회
	    model.addAttribute("dto", dto);

	    return "review/reviewview";
	}
	
	@Value("${kakao.api.key}")
	private String kakaoAppKey;

	@GetMapping("/review/reviewadd")
	public String reviewAdd(Model model) {
		
	    model.addAttribute("kakaoAppKey", kakaoAppKey);
	    
	    return "review/reviewadd";
	}
	
	@PostMapping("/review/reviewaddok")
	public String reviewaddok(BoardReviewDTO dto, Principal principal) {

	    Long id = (principal != null) ? Long.parseLong(principal.getName()) : 1L;
	    dto.setSeqUser(id);

	    MultipartFile file = dto.getFile(); // ✅ MultipartFile로 받아야 함
	    String fileName = null;

	    if (file != null && !file.isEmpty()) {
	        fileName = file.getOriginalFilename();
	        dto.setAttach(fileName); // attach 필드에 파일명 저장

	        // 실제 저장: File savePath = new File(uploadDir, uuid + "_" + fileName);
	        // file.transferTo(savePath);
	    }

	    Long newReviewId = reviewService.add(dto);
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
