package com.project.yamipick.review.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {
	
	private final S3Uploader s3Uploader;
	private final ReviewService reviewService;
	private final ReviewStoreService reviewStoreService;
	
	//임시 로그인
	private String getLoginUsername(Principal principal) {
	    if (principal == null) {
	        return null;
	    }
	    return principal.getName(); // ex) testUser
	}
	
	@GetMapping("/review/reviewmain")
	public String reviewmain(HttpServletRequest request, Model model, Principal principal) {
		
		List<BoardReviewDTO> recentReviews = new ArrayList<>();

	    if (request.getCookies() != null) {
	        for (Cookie c : request.getCookies()) {
	            if ("recentReviews".equals(c.getName())) {
	                String[] ids = c.getValue().split(",");
	                recentReviews = reviewService.getReviewsByIds(ids);
	            }
	        }
	    }
	    
	    // 로그인 사용자 ID (없으면 null)
	    String username = getLoginUsername(principal);
	    model.addAttribute("loginUsername", username);

	    if (username != null) {
	        model.addAttribute("activity",
	            reviewService.getMyActivitySummary(username));
	    }

	    model.addAttribute("recentReviews", recentReviews);
		
		model.addAttribute("recommendReviews", reviewService.getRecommendReviews());

	    model.addAttribute("photoReviews", reviewService.getPhotoReviews());

	    model.addAttribute("nearReviews", reviewService.getNearReviews(/* lat, lng */));

	    model.addAttribute("popularDaily", reviewService.getPopularDaily());

	    model.addAttribute("popularWeekly", reviewService.getPopularWeekly());

	    return "review/reviewmain";
	}
	
	@GetMapping("/review/reviewlist")
	public String reviewlist(@RequestParam(name="keyword", required=false) String keyword,
            				 @RequestParam(name="sort", defaultValue="latest") String sort,
            				 @RequestParam(name="page", defaultValue="0") int page,
							 Model model, Principal principal, HttpServletRequest request) {
		
		// 목록 조회 (검색/정렬/페이징 통합)
	    List<BoardReviewDTO> list = reviewService.getList(keyword, sort, page);
	    
	    List<BoardReviewDTO> recentReviews = new ArrayList<>();

	    if (request.getCookies() != null) {
	        for (Cookie c : request.getCookies()) {
	            if ("recentReviews".equals(c.getName())) {
	                String[] ids = c.getValue().split(",");
	                recentReviews = reviewService.getReviewsByIds(ids);
	            }
	        }
	    }
	    
	    // 로그인 사용자 ID (없으면 null)
	    String username = getLoginUsername(principal);
	    model.addAttribute("loginUsername", username);

	    if (username != null) {
	        model.addAttribute("activity",
	            reviewService.getMyActivitySummary(username));
	    }

	    model.addAttribute("recentReviews", recentReviews);

	    model.addAttribute("list", list);
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("sort", sort);

	    // 사이드바
	    
	    model.addAttribute("recommendReviews",
	            reviewService.getRecommendReviews());

	    model.addAttribute("photoReviews",
	            reviewService.getPhotoReviews());

	    model.addAttribute("popularDaily",
	            reviewService.getPopularDaily());

	    model.addAttribute("popularWeekly",
	            reviewService.getPopularWeekly());

	    return "review/reviewlist";
	}
	
	@GetMapping("/review/activity")
	public String activity(@RequestParam(name="tab", defaultValue = "review") String tab,
	                       Model model, Principal principal, HttpServletRequest request) {

		List<BoardReviewDTO> recentReviews = new ArrayList<>();

	    if (request.getCookies() != null) {
	        for (Cookie c : request.getCookies()) {
	            if ("recentReviews".equals(c.getName())) {
	                String[] ids = c.getValue().split(",");
	                recentReviews = reviewService.getReviewsByIds(ids);
	            }
	        }
	    }

	    model.addAttribute("recentReviews", recentReviews);
		
	    String username = getLoginUsername(principal);
	    if (username == null) {
	        return "redirect:/user/login";
	    }
	    

	    switch (tab) {
	        case "review":
	            model.addAttribute("list", reviewService.getMyReviews(username));
	            break;

	        case "comment":
	            model.addAttribute("list", reviewService.getMyComments(username));
	            break;

	        case "favorite":
	            model.addAttribute("list", reviewService.getMyFavorites(username));
	            break;

	        case "scrap":
	            model.addAttribute("list", reviewService.getMyScraps(username));
	            break;
	    }
	    
	    model.addAttribute("recommendReviews",
	            reviewService.getRecommendReviews());

	    model.addAttribute("popularDaily",
	            reviewService.getPopularDaily());

	    model.addAttribute("popularWeekly",
	            reviewService.getPopularWeekly());

	    model.addAttribute("tab", tab);

	    return "review/activity"; // 하나의 템플릿만 사용
	}
	
	@PostMapping("/review/reviewaddok")
	public String reviewaddok(BoardReviewDTO dto, Principal principal, Model model) {

		String username = getLoginUsername(principal);
		dto.setUserId(username); // DTO에 String userId 필드 있어야 함

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
	public String reviewview(BoardReviewDTO rdto, @RequestParam("seqReview") Long seqReview, Principal principal, Model model, HttpServletRequest request,
            HttpServletResponse response) {

		String username = getLoginUsername(principal);
		model.addAttribute("loginUsername", username);
	    
	    saveRecentReview(seqReview, request, response);
		
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
	    model.addAttribute("comments", comments);
	    model.addAttribute("commentCount",
	            reviewService.getCommentCount(seqReview));
	    model.addAttribute("isFavorite", reviewService.isFavorite(seqReview, username));
	    model.addAttribute("isScrap", reviewService.isScrap(seqReview, username));


	    return "review/reviewview";
	}
	
	private void saveRecentReview(Long seqReview,
            HttpServletRequest request,
            HttpServletResponse response) {

		String cookieName = "recentReviews";
		String value = "";
		
		if (request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (cookieName.equals(c.getName())) {
					value = c.getValue();
				}
			}
		}
		
		List<String> list = new ArrayList<>();
		
		if (!value.isEmpty()) {
			list.addAll(Arrays.asList(value.split("\\|")));
		}
		
		// 중복 제거
		list.remove(seqReview.toString());
		// 앞에 추가
		list.add(0, seqReview.toString());
		
		// 최대 5개
		if (list.size() > 5) {
			list = list.subList(0, 5);
		}
		
		Cookie cookie = new Cookie(cookieName, String.join("|", list));
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24 * 7); // 7일
		response.addCookie(cookie);
	}

	@Value("${yamipick.api.kakao.js-key}")
	private String kakaoAppKey;

	@GetMapping("/review/reviewadd")
	public String reviewAdd(Model model) {
		
	    model.addAttribute("kakaoAppKey", kakaoAppKey);
	    
	    return "review/reviewadd";
	}
	
	@PostMapping("/review/comment/add")
	@ResponseBody
	public CommentDTO addComment(CommentDTO dto, Principal principal) {

		String username = getLoginUsername(principal);
		dto.setUserId(username);

	    return reviewService.addComment(dto);
	}
	
	@PostMapping("/review/favorite/toggle")
	@ResponseBody
	public Map<String, Object> toggleFavorite(@RequestParam("seqReview") Long seqReview, Principal principal) {

		String username = getLoginUsername(principal);

	    boolean isFavorite = reviewService.toggleFavorite(seqReview, username);

	    Map<String, Object> res = new HashMap<>();
	    res.put("favorite", isFavorite);
	    res.put("count", reviewService.getFavoriteCount(seqReview));

	    return res;
	}
	
	@PostMapping("/review/scrap/toggle")
	@ResponseBody
	public Map<String, Object> toggleScrap(@RequestParam("seqReview") Long seqReview, Principal principal) {

		String username = getLoginUsername(principal);

	    boolean isScrap = reviewService.toggleScrap(seqReview, username);

	    Map<String, Object> res = new HashMap<>();
	    res.put("scrap", isScrap);

	    return res;
	}
	
	@GetMapping("/review/reviewedit")
	public String reviewEdit(@RequestParam("seqReview") Long seqReview, Model model) {
		
	    BoardReviewDTO dto = reviewService.getReviewForEdit(seqReview);
	    
	    model.addAttribute("review", dto);
	    model.addAttribute("kakaoAppKey", kakaoAppKey);
	    
	    return "review/reviewedit";
	}
	
	@PostMapping("/review/revieweditok")
	public String reviewEditOk(BoardReviewDTO dto, Principal principal) {

		String username = getLoginUsername(principal);
		dto.setUserId(username);

	    reviewService.edit(dto);
	    return "redirect:/review/reviewview?seqReview=" + dto.getSeqReview();
	}
	
	@PostMapping("/review/reviewdeleteok")
	public String reviewDeleteOk(
	        @RequestParam("seqReview") Long seqReview,
	        Principal principal) {
		
		String username = getLoginUsername(principal);

	    reviewService.deleteReview(seqReview, username);

	    return "redirect:/review/reviewlist";
	}
	
	@PostMapping("/review/comment/edit")
	@ResponseBody
	public Map<String, Object> editComment(
	        @RequestParam("seqComment") Long seqComment,
	        @RequestParam("content") String content,
	        Principal principal) {

		String username = getLoginUsername(principal);

	    CommentDTO updated = reviewService.editComment(seqComment, username, content);

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

		String username = getLoginUsername(principal);

	    boolean success = reviewService.deleteComment(seqComment, username);

	    Map<String, Object> res = new HashMap<>();
	    res.put("success", success);
	    return res;
	}

}
