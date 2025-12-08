package com.project.yamipick.review.service;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;
import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.repository.BoardReviewRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final BoardReviewRepository boardReviewRepository;
    private final UserRepository userRepository; // 사용자 정보 가져올 때 필요

    @Override
    public Long add(BoardReviewDTO dto) {

        User user = userRepository.findById(dto.getSeqUser())
                                  .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        BoardReview review = BoardReview.builder()
                .title(dto.getTitle())
                .starRating(dto.getStarRating())
                .user(user)
                .reviewContent(dto.getReviewContent())
                .attach(dto.getAttach())
                .place(dto.getPlace())
                .regdate(new Date(System.currentTimeMillis()))
                .readCount(0)
                .favoriteCount(0)
                .contentState(dto.getContentState())
                .build();

        BoardReview saved = boardReviewRepository.save(review);

        return saved.getSeqReview(); // ID 반환
    }
	
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

	@Override
	public BoardReviewDTO getReview(Long seqReview) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
