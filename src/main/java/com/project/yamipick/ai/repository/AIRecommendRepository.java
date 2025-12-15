package com.project.yamipick.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.yamipick.ai.entity.AIRecommend;

public interface AIRecommendRepository extends JpaRepository<AIRecommend, Long> {

	List<AIRecommend> findBySeqSession(Long seqSession);
	
}
