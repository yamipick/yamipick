package com.project.yamipick.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.ai.entity.AIReviewSummary;

public interface AIReviewSummaryRepository extends JpaRepository<AIReviewSummary, Long> {
	
	Optional<AIReviewSummary> findByRestaurantId(String restaurantId);
}
