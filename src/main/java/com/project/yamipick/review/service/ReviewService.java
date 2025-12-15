package com.project.yamipick.review.service;

import java.util.List;
import java.util.Map;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;

public interface ReviewService {
	
	List<BoardReviewDTO> getMyReviews(String username);

    List<CommentDTO> getMyComments(String username);

    List<BoardReviewDTO> getMyFavorites(String username);

    List<BoardReviewDTO> getMyScraps(String username);

	Long add(BoardReviewDTO dto);

	BoardReviewDTO getReview(Long seqReview);

	List<CommentDTO> getComments(Long seqReview);

	CommentDTO addComment(CommentDTO dto);
	
	boolean toggleFavorite(Long seqReview, String username);
	
	boolean toggleScrap(Long seqReview, String username);
	
	boolean isFavorite(Long seqReview, String username);

	boolean isScrap(Long seqReview, String username);
	
	int getFavoriteCount(Long seqReview);

	CommentDTO editComment(Long seqComment, String username, String content);

	boolean deleteComment(Long seqComment, String username);

	int getCommentCount(Long seqReview);

	BoardReviewDTO getReviewForEdit(Long seqReview);

	void edit(BoardReviewDTO dto);

	void deleteReview(Long seqReview, String username);

	List<BoardReviewDTO> getPopularDaily();

	List<BoardReviewDTO> getPopularWeekly();
	
	List<BoardReviewDTO> getRecommendReviews();

	List<BoardReviewDTO> getPhotoReviews();

	List<BoardReviewDTO> getNearReviews(); // 위치는 나중에 확장

	Map<String, Long> getMyActivitySummary(String username);

	List<BoardReviewDTO> findAll();

	List<BoardReviewDTO> getList(String keyword, String sort, int page);

	List<BoardReviewDTO> getReviewsByIds(String[] ids);


}
