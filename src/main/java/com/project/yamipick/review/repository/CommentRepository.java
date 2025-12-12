package com.project.yamipick.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.review.entity.Comment;
import com.project.yamipick.user.entity.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByReviewSeqReviewOrderByRegdateAsc(Long seqReview);
    
    List<Comment> findByReviewSeqReviewOrderBySeqCommentAsc(Long seqReview);
    
    int countByReviewSeqReviewAndState(Long seqReview, String string);
    
    long countByUserAndState(User user, String state);

}