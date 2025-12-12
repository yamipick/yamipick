package com.project.yamipick.review.service;

import java.util.List;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;

public interface ReviewService {
	
	List<BoardReviewDTO> getMyReviews(Long seqUser);

    List<CommentDTO> getMyComments(Long seqUser);

    List<FavoriteReviewDTO> getMyFavorites(Long seqUser);

    List<ScrapReviewDTO> getMyScraps(Long seqUser);

	Long add(BoardReviewDTO dto);

	BoardReviewDTO getReview(Long seqReview);

	List<CommentDTO> getComments(Long seqReview);

	CommentDTO addComment(CommentDTO dto);
	
	boolean toggleFavorite(Long seqReview, Long seqUser);
	
	boolean toggleScrap(Long seqReview, Long seqUser);
	
	boolean isFavorite(Long seqReview, Long seqUser);

	boolean isScrap(Long seqReview, Long seqUser);
	
	int getFavoriteCount(Long seqReview);

	CommentDTO editComment(Long seqComment, Long seqUser, String content);

	boolean deleteComment(Long seqComment, Long seqUser);

	int getCommentCount(Long seqReview);

	BoardReviewDTO getReviewForEdit(Long seqReview);

	void edit(BoardReviewDTO dto);

	void deleteReview(Long seqReview, Long seqUser);

}
