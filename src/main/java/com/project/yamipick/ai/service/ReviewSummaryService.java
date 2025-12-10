package com.project.yamipick.ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.ReviewSummaryResponse;
import com.project.yamipick.ai.entity.AIReviewSummary;
import com.project.yamipick.ai.repository.AIReviewSummaryRepository;
import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.repository.BoardReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

	private final AIReviewSummaryRepository summaryRepo;
	private final BoardReviewRepository reviewRepo;
	private final GeminiService gemini;       
	
	public ReviewSummaryResponse summarize(String restaurantId) {
		
		//TODO: 리뷰기능 완성 후 실제 리뷰 가져오기
		List<BoardReview> reviews = reviewRepo.findAll(); //임시
		if (reviews.isEmpty()) {
			throw new IllegalStateException("리뷰가 없습니다.");
		}
		
		//리뷰 텍스트 합치기
		String mergedReviews = reviews.stream()
		        .map(r -> "임시 리뷰 내용")   // TODO: 리뷰 엔티티 완성 후 필드로 변경
		        .collect(Collectors.joining("\n"));
		
		//기존 요약 존재 여부 확인
		AIReviewSummary existing = summaryRepo.findByRestaurantId(restaurantId).orElse(null);
		
		//AI 호출
        String publicPrompt = """
                아래 리뷰 내용을 기반으로 2~3문장 "짧은 요약"을 만들어줘.
                리뷰:
                %s
                """.formatted(mergedReviews);

        String summaryPublic = gemini.call(publicPrompt).block();

        String memberPrompt = """
                아래 리뷰 내용을 기반으로 4~6문장 "상세 요약"을 만들어줘.
                장점, 단점, 자주 언급된 내용이 드러나도록 작성해줘.
                리뷰:
                %s
                """.formatted(mergedReviews);

        String summaryMember = gemini.call(memberPrompt).block();
		
		//기존 있으면 업데이트
        if (existing != null) {
            existing.update(summaryPublic, summaryMember);
            summaryRepo.save(existing);
            return ReviewSummaryResponse.of(existing);
        }
		
		//신규 저장
        AIReviewSummary created = AIReviewSummary.create(
                restaurantId, summaryPublic, summaryMember
        );
		
		summaryRepo.save(created);
		return ReviewSummaryResponse.of(created);
		
	}
	
}
