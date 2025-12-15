package com.project.yamipick.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.FavoriteReview;
import com.project.yamipick.review.entity.ScrapReview;
import com.project.yamipick.user.entity.User;

public interface ScrapReviewRepository extends JpaRepository<ScrapReview, Long> {

	Optional<ScrapReview> findByUser_UserIdAndReviewSeqReview(String username, Long seqReview);

	boolean existsByUser_UserIdAndReviewSeqReview(String username, Long seqReview);

	void deleteByReview(BoardReview review);

	long countByUser_UserId(String username);
	
	List<ScrapReview>
	findByUser_UserIdAndReview_StateOrderByRegdateDesc(
            String username,
            String state
    );

}