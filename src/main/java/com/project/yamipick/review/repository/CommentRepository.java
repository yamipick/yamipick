package com.project.yamipick.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}