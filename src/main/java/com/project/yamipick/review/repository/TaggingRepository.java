package com.project.yamipick.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Tagging;

public interface TaggingRepository extends JpaRepository<Tagging, Long> {

	List<Tagging> findByReview(BoardReview review);

	void deleteByReview(BoardReview review);

}