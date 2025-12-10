package com.project.yamipick.review.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;
import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Comment;
import com.project.yamipick.review.entity.Hashtag;
import com.project.yamipick.review.entity.Tagging;
import com.project.yamipick.review.repository.BoardReviewRepository;
import com.project.yamipick.review.repository.CommentRepository;
import com.project.yamipick.review.repository.HashtagRepository;
import com.project.yamipick.review.repository.TaggingRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

	private final BoardReviewRepository boardReviewRepository;
    private final UserRepository userRepository; // 사용자 정보 가져올 때 필요
    private final HashtagRepository hashtagRepository;
    private final TaggingRepository taggingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long add(BoardReviewDTO dto) {

        // 0. 글 유형 기본값 처리
        String state = dto.getContentState();
        if (state == null || state.isEmpty()) {
            state = "일반글";
        }

        // 1. 작성자 조회 (orElseGet 안 씀)
        User user = userRepository.findById(dto.getSeqUser())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 2. 리뷰 엔티티 생성 (Store는 지금 단계에서 안 건드림)
        BoardReview review = BoardReview.builder()
                .title(dto.getTitle())
                .starRating(dto.getStarRating())
                .user(user)                     // FK: 작성자
                .reviewContent(dto.getReviewContent())
                .attach(dto.getAttach())
                .place(dto.getPlace())          // "이름|위도|경도|placeId" 그대로 저장
                .regdate(new Date(System.currentTimeMillis()))
                .readCount(0)
                .favoriteCount(0)
                .contentState(state)
                .build();

        // 3. 리뷰 저장
        BoardReview saved = boardReviewRepository.save(review);

        // 4. 태그 저장
        String tags = dto.getTags();
        if (tags != null && !tags.isBlank()) {

            for (String raw : tags.split(",")) {
                String name = raw.trim();
                if (name.isEmpty()) continue;

                // orElseGet 안 쓰는 버전
                Hashtag hashtag = hashtagRepository.findByHashtag(name)
                        .orElse(null);

                if (hashtag == null) {
                    hashtag = hashtagRepository.save(
                            Hashtag.builder()
                                    .hashtag(name)
                                    .build()
                    );
                }

                taggingRepository.save(
                        Tagging.builder()
                                .review(saved)
                                .hashtag(hashtag)
                                .build()
                );
            }
        }

        return saved.getSeqReview();
    }
    
    @Override
    public List<CommentDTO> getComments(Long seqReview) {
        List<Comment> raw = commentRepository
                .findByReviewSeqReviewOrderBySeqCommentAsc(seqReview);

        // 부모ID -> 자식 List 로 맵 구성
        Map<Long, List<CommentDTO>> childrenMap = new HashMap<>();

        List<CommentDTO> roots = new ArrayList<>();

        for (Comment c : raw) {
            CommentDTO dto = c.toDTO();

            Long parentId = (c.getParentComment() != null)
                    ? c.getParentComment().getSeqComment()
                    : null;

            if (parentId == null) {
                roots.add(dto);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(dto);
            }
        }

        // 최종 출력 리스트
        List<CommentDTO> ordered = new ArrayList<>();

        for (CommentDTO root : roots) {
            ordered.add(root); // 부모 먼저
            addChildrenRecursively(ordered, root, childrenMap);
        }

        return ordered;
    }

    private void addChildrenRecursively(
            List<CommentDTO> result,
            CommentDTO parent,
            Map<Long, List<CommentDTO>> childrenMap) {

        List<CommentDTO> children = childrenMap.get(parent.getSeqComment());
        if (children == null) return;
        
        children.sort(Comparator.comparing(CommentDTO::getSeqComment));
        
        for (CommentDTO child : children) {
            child.setDepth(parent.getDepth() + 1); // 들여쓰기용
            result.add(child);
            addChildrenRecursively(result, child, childrenMap); // 재귀로 모든 깊이 처리
        }
    }
    
    @Override
    @Transactional
    public CommentDTO addComment(CommentDTO dto) {

        User user = userRepository.findById(dto.getSeqUser())
                .orElseThrow();
        BoardReview review = boardReviewRepository.findById(dto.getSeqReview())
                .orElseThrow();
        
        Comment parent = null;
        if (dto.getSeqParentComment() != null) {
            parent = commentRepository.findById(dto.getSeqParentComment()).orElse(null);
        }

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .regdate(Timestamp.valueOf(LocalDateTime.now()))
                .user(user)
                .review(review)
                .parentComment(parent)
                .build();

        Comment saved = commentRepository.save(comment);

        // JSON으로 돌려줄 DTO 생성
        CommentDTO result = new CommentDTO();
        result.setNickname(user.getNickname());
        result.setContent(saved.getContent());
        result.setRegdate(saved.getRegdate());
        result.setSeqUser(user.getSeqUser());
        result.setSeqReview(saved.getReview().getSeqReview());
        result.setSeqParentComment(
                saved.getParentComment() != null ? saved.getParentComment().getSeqComment() : null
            );

        return result;  
    }
	
	@Override
	public List<BoardReviewDTO> getMyReviews(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommentDTO> getMyComments(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FavoriteReviewDTO> getMyFavorites(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScrapReviewDTO> getMyScraps(Long userSeq) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoardReviewDTO getReview(Long seqReview) {

	    // 1. 리뷰 엔티티 조회
	    BoardReview review = boardReviewRepository.findById(seqReview)
	            .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

	    // 2. 조회수 증가
	    review.setReadCount(review.getReadCount() + 1);
	    boardReviewRepository.save(review);

	    // 3. 엔티티 → DTO 변환
	    BoardReviewDTO dto = review.toDTO();
	    
	    // ★ 조회수 증가 후 값을 다시 DTO에 넣기 (안 넣으면 null 또는 이전값)
	    dto.setReadCount(review.getReadCount());

	    // 4. 태그 리스트 삽입
	    dto.setTagList(
	            taggingRepository.findByReview(review)
	                    .stream()
	                    .map(t -> t.getHashtag().getHashtag())
	                    .toList()
	    );

	    return dto;
	}
	

}
