package com.project.yamipick.review.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;

@Service
public class ReviewServiceImpl implements ReviewService {

	@Override
	public List<BoardReviewDTO> getMyReviews(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommentDTO> getMyComments(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FavoriteReviewDTO> getMyFavorites(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScrapReviewDTO> getMyScraps(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
