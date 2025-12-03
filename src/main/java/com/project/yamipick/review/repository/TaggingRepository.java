package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.Tagging;

public interface TaggingRepository extends JpaRepository<Tagging, Long> {

}