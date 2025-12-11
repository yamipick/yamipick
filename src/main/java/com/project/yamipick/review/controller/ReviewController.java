package com.project.yamipick.review.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.aws.S3Uploader;
import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.service.ReviewService;
import com.project.yamipick.store.service.ReviewStoreService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {
	
	private final S3Uploader s3Uploader;
	private final ReviewService reviewService;
	private final ReviewStoreService reviewStoreService;
	
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
	
	@PostMapping("/review/reviewaddok")
	public String reviewaddok(BoardReviewDTO dto, Principal principal, Model model) {

	    Long id = (principal != null) ? Long.parseLong(principal.getName()) : 1L;
	    dto.setSeqUser(id);

	    MultipartFile file = dto.getFile(); // ✅ MultipartFile로 받아야 함

	    if (file != null && !file.isEmpty()) {
	    	String imageUrl = null;
			try {
				imageUrl = s3Uploader.upload(file, "review");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        // ★ DB에 저장할 attach -> 이제 파일명 말고 "URL"
	        dto.setAttach(imageUrl);
	    }
	    
	    if ("비밀글".equals(dto.getContentState())) {
	        dto.setContentState("비밀글");
	    } else {
	        // 체크 안 했으면 DB 기본값(일반글) 사용
	        dto.setContentState(null);
	    }
	    
	    // 태그
	    if (dto.getTags() != null) {
	        dto.setTagList(Arrays.asList(dto.getTags().split(",")));
	    }

	    Long seq = reviewService.add(dto);
	    return "redirect:/review/reviewview?seqReview=" + seq;
	}
	
	@GetMapping("/review/reviewview")
	public String reviewview(BoardReviewDTO rdto, @RequestParam("seqReview") Long seqReview, Principal principal, Model model) {

		Long seqUser = (principal != null) ? Long.parseLong(principal.getName()) : 1L;
	    rdto.setSeqUser(seqUser);
		
	    BoardReviewDTO dto = reviewService.getReview(seqReview);
	    model.addAttribute("review", dto);
	    model.addAttribute("kakaoAppKey", kakaoAppKey);

	    Double lat = null;
	    Double lng = null;
	    String placeName = null;
	    String kakaoPlaceId = null;

	    // 1) place 문자열 파싱
	    String placeStr = dto.getPlace();
	    if (placeStr != null && !placeStr.isBlank()) {
	        if (placeStr.contains("|")) {
	            String[] arr = placeStr.split("\\|");
	            placeName = arr[0];
	            lat = Double.parseDouble(arr[1]);
	            lng = Double.parseDouble(arr[2]);
	            if (arr.length > 3) {
	                kakaoPlaceId = arr[3];
	            }
	        } else if (placeStr.contains(",")) { // 예전 데이터 대비
	            String[] arr = placeStr.split(",");
	            lat = Double.parseDouble(arr[0]);
	            lng = Double.parseDouble(arr[1]);
	        }
	    }

	    if (lat != null && lng != null) {
	        model.addAttribute("lat", lat);
	        model.addAttribute("lng", lng);
	    }
	    model.addAttribute("name", placeName);
	    model.addAttribute("kakaoPlaceId", kakaoPlaceId);
	    
	    var comments = reviewService.getComments(seqReview);
	    
	    model.addAttribute("comments", reviewService.getComments(seqReview));
	    model.addAttribute("commentCount", comments.size());
	    model.addAttribute("isFavorite", reviewService.isFavorite(seqReview, seqUser));
	    model.addAttribute("isScrap", reviewService.isScrap(seqReview, seqUser));

	    return "review/reviewview";
	}
	
	@Value("${kakao.api.key}")
	private String kakaoAppKey;

	@GetMapping("/review/reviewadd")
	public String reviewAdd(Model model) {
		
	    model.addAttribute("kakaoAppKey", kakaoAppKey);
	    
	    return "review/reviewadd";
	}
	
	@PostMapping("/review/comment/add")
	@ResponseBody
	public CommentDTO addComment(CommentDTO dto, Principal principal) {

	    Long id = (principal != null) ? Long.parseLong(principal.getName()) : 1L;
	    dto.setSeqUser(id);

	    return reviewService.addComment(dto);
	}
	
	@PostMapping("/review/favorite/toggle")
	@ResponseBody
	public Map<String, Object> toggleFavorite(@RequestParam("seqReview") Long seqReview, Principal principal) {

	    Long seqUser = (principal != null) ? Long.parseLong(principal.getName()) : 1L;

	    boolean isFavorite = reviewService.toggleFavorite(seqReview, seqUser);

	    Map<String, Object> res = new HashMap<>();
	    res.put("favorite", isFavorite);
	    res.put("count", reviewService.getFavoriteCount(seqReview));

	    return res;
	}
	
	@PostMapping("/review/scrap/toggle")
	@ResponseBody
	public Map<String, Object> toggleScrap(@RequestParam("seqReview") Long seqReview, Principal principal) {

	    Long seqUser = (principal != null) ? Long.parseLong(principal.getName()) : 1L;

	    boolean isScrap = reviewService.toggleScrap(seqReview, seqUser);

	    Map<String, Object> res = new HashMap<>();
	    res.put("scrap", isScrap);

	    return res;
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
	
	@PostMapping("/review/comment/edit")
	@ResponseBody
	public Map<String, Object> editComment(
	        @RequestParam("seqComment") Long seqComment,
	        @RequestParam("content") String content,
	        Principal principal) {

	    Long seqUser = (principal != null) ? Long.parseLong(principal.getName()) : 1L;

	    CommentDTO updated = reviewService.editComment(seqComment, seqUser, content);

	    Map<String, Object> res = new HashMap<>();
	    res.put("content", updated.getContent());
	    res.put("regdate", updated.getRegdate());

	    return res;
	}

	@PostMapping("/review/comment/delete")
	@ResponseBody
	public Map<String, Object> deleteComment(
	        @RequestParam("seqComment") Long seqComment,
	        Principal principal) {

	    Long seqUser = (principal != null) ? Long.parseLong(principal.getName()) : 1L;

	    boolean success = reviewService.deleteComment(seqComment, seqUser);

	    Map<String, Object> res = new HashMap<>();
	    res.put("success", success);
	    return res;
	}

}
