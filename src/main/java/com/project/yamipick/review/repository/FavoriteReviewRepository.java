package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.FavoriteReview;

public interface FavoriteReviewRepository extends JpaRepository<FavoriteReview, Long> {

}