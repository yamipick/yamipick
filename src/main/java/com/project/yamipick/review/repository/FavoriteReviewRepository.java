package com.project.yamipick.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.FavoriteReview;
import com.project.yamipick.user.entity.User;

public interface FavoriteReviewRepository extends JpaRepository<FavoriteReview, Long> {
	
	int countByReviewSeqReview(Long seqReview);

	Optional<FavoriteReview> findByUserSeqUserAndReviewSeqReview(Long seqUser, Long seqReview);

	boolean existsByUserSeqUserAndReviewSeqReview(Long seqUser, Long seqReview);

	void deleteByReview(BoardReview review);

	long countByUser(User user);
	
	List<FavoriteReview>
    findByUser_SeqUserAndReview_StateOrderByRegdateDesc(
            Long seqUser,
            String state
    );

}