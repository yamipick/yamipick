package com.project.yamipick.review.service;

import java.sql.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;
import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Hashtag;
import com.project.yamipick.review.entity.Tagging;
import com.project.yamipick.review.repository.BoardReviewRepository;
import com.project.yamipick.review.repository.HashtagRepository;
import com.project.yamipick.review.repository.TaggingRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final BoardReviewRepository boardReviewRepository;
    private final UserRepository userRepository; // 사용자 정보 가져올 때 필요
    private final HashtagRepository hashtagRepository;
    private final TaggingRepository taggingRepository;

    @Override
    public Long add(BoardReviewDTO dto) {
    	
    	String state = dto.getContentState();
    	if (state == null || state.isEmpty()) {
    	    state = "일반글";
    	}

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
                .contentState(state)
                .build();

        BoardReview saved = boardReviewRepository.save(review);
        
        String tags = dto.getTags();  // "매운맛,데이트,분위기좋음" 이런 문자열
        if (tags != null && !tags.isBlank()) {

            for (String raw : tags.split(",")) {
                String name = raw.trim();
                if (name.isEmpty()) continue;

                // 1) 기존 태그 있으면 조회, 없으면 생성
                Hashtag hashtag = hashtagRepository.findByHashtag(name)
                        .orElse(null);

                if (hashtag == null) {
                    hashtag = hashtagRepository.save(
                            Hashtag.builder()
                                    .hashtag(name)
                                    .build()
                    );
                }

                // 2) 매핑(Tagging) 생성
                Tagging tagging = Tagging.builder()
                        .review(saved)
                        .hashtag(hashtag)
                        .build();

                taggingRepository.save(tagging);
            }
        }


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
