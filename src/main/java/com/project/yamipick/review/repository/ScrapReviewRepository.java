package com.project.yamipick.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.FavoriteReview;
import com.project.yamipick.review.entity.ScrapReview;
import com.project.yamipick.user.entity.User;

public interface ScrapReviewRepository extends JpaRepository<ScrapReview, Long> {

	Optional<ScrapReview> findByUserSeqUserAndReviewSeqReview(Long seqUser, Long seqReview);

	boolean existsByUserSeqUserAndReviewSeqReview(Long seqUser, Long seqReview);

	void deleteByReview(BoardReview review);

	long countByUser(User user);
	
	List<ScrapReview>
    findByUser_SeqUserAndReview_StateOrderByRegdateDesc(
            Long seqUser,
            String state
    );

}