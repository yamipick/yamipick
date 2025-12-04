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

}
