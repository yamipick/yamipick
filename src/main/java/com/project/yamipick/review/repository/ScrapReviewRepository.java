package com.project.yamipick.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.ScrapReview;

public interface ScrapReviewRepository extends JpaRepository<ScrapReview, Long> {

	Optional<ScrapReview> findByUserSeqUserAndReviewSeqReview(Long seqUser, Long seqReview);

	boolean existsByUserSeqUserAndReviewSeqReview(Long seqUser, Long seqReview);

}