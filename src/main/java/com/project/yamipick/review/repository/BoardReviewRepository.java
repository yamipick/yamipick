package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.BoardReview;

public interface BoardReviewRepository extends JpaRepository<BoardReview, Long> {

}
