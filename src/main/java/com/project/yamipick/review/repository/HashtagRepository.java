package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

}