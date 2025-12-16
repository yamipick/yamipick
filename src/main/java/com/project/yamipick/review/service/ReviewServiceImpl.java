package com.project.yamipick.review.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.yamipick.aws.S3Uploader;
import com.project.yamipick.review.dto.BoardReviewDTO;
import com.project.yamipick.review.dto.CommentDTO;
import com.project.yamipick.review.dto.FavoriteReviewDTO;
import com.project.yamipick.review.dto.ScrapReviewDTO;
import com.project.yamipick.review.entity.BoardReview;
import com.project.yamipick.review.entity.Comment;
import com.project.yamipick.review.entity.FavoriteReview;
import com.project.yamipick.review.entity.Hashtag;
import com.project.yamipick.review.entity.ScrapReview;
import com.project.yamipick.review.entity.Tagging;
import com.project.yamipick.review.repository.BoardReviewRepository;
import com.project.yamipick.review.repository.CommentRepository;
import com.project.yamipick.review.repository.FavoriteReviewRepository;
import com.project.yamipick.review.repository.HashtagRepository;
import com.project.yamipick.review.repository.ScrapReviewRepository;
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
    private final FavoriteReviewRepository favoriteReviewRepository;
    private final ScrapReviewRepository scrapReviewRepository;
    private final S3Uploader s3Uploader;

    @Override
    public List<BoardReviewDTO> getPopularDaily() {
        return boardReviewRepository
            .findPopular(LocalDateTime.now().minusDays(1),
                         PageRequest.of(0, 5))
            .stream().map(BoardReview::toDTO)
            .toList();
    }
    
    @Override
    public List<BoardReviewDTO> getPopularWeekly() {

        LocalDateTime from = LocalDateTime.now().minusDays(7);
        PageRequest page = PageRequest.of(0, 5);

        return boardReviewRepository
                .findPopular(from, page)
                .stream()
                .map(BoardReview::toDTO)
                .toList();
    }
    
    @Override
    public List<BoardReviewDTO> getList(String keyword, String sort, int page) {

        Sort sortOption;

        switch (sort) {
            case "views":
                sortOption = Sort.by(Sort.Direction.DESC, "readCount");
                break;
            case "favoritest":
                sortOption = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
            case "latest":
            default:
                sortOption = Sort.by(Sort.Direction.DESC, "regdate");
        }

        Pageable pageable = PageRequest.of(page, 20, sortOption);

        List<BoardReview> result;

        if (keyword == null || keyword.isBlank()) {
            result = boardReviewRepository.findByState("ACTIVE", pageable);
        } else {
            result = boardReviewRepository.search(keyword, pageable);
        }

        return result.stream()
                .map(BoardReview::toDTO)
                .toList();
    }
    
    @Override
    public List<BoardReviewDTO> getRecommendReviews() {
        return boardReviewRepository
                .findRecommendReviews(PageRequest.of(0, 5))
                .stream()
                .map(BoardReview::toDTO)
                .toList();
    }
    
    @Override
    public Map<String, Long> getMyActivitySummary(String username) {

        Map<String, Long> map = new HashMap<>();

        map.put("review",
                boardReviewRepository.countByUser_UserIdAndState(username, "ACTIVE"));

        map.put("comment",
                commentRepository.countByUser_UserIdAndState(username, "ACTIVE"));

        map.put("favorite",
                favoriteReviewRepository.countByUser_UserId(username));

        map.put("scrap",
                scrapReviewRepository.countByUser_UserId(username));

        return map;
    }
    
    @Override
    public List<BoardReviewDTO> getReviewsByIds(String[] ids) {

        if (ids == null || ids.length == 0) return List.of();

        List<Long> reviewIds = Arrays.stream(ids)
        		.flatMap(s -> Arrays.stream(s.split("\\|")))
                .filter(v -> !v.isBlank())
                .map(Long::parseLong)
                .toList();

        List<BoardReview> entities =
                boardReviewRepository.findBySeqReviewIn(reviewIds);

        return entities.stream()
                .map(BoardReview::toDTO)
                .toList();
    }


    @Override
    public List<BoardReviewDTO> getPhotoReviews() {
        return boardReviewRepository
                .findPhotoReviews(PageRequest.of(0, 6))
                .stream()
                .map(BoardReview::toDTO)
                .toList();
    }

    @Override
    public List<BoardReviewDTO> getNearReviews() {
        // 위치 기반은 나중에
        return boardReviewRepository
                .findRecommendReviews(PageRequest.of(0, 5))
                .stream()
                .map(BoardReview::toDTO)
                .toList();
    }
    
    @Override
    public List<BoardReviewDTO> findAll() {
        return boardReviewRepository.findAll()
                .stream()
                .map(BoardReview::toDTO)
                .toList();
    }

    @Override
    public Long add(BoardReviewDTO dto) {

        // 0. 글 유형 기본값 처리
        String state = dto.getContentState();
        if (state == null || state.isEmpty()) {
            state = "일반글";
        }

        // 1. 작성자 조회 (orElseGet 안 씀)
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 2. 리뷰 엔티티 생성 (Store는 지금 단계에서 안 건드림)
        BoardReview review = BoardReview.builder()
                .title(dto.getTitle())
                .starRating(dto.getStarRating())
                .user(user)                     // FK: 작성자
                .reviewContent(dto.getReviewContent())
                .attach(dto.getAttach())
                .place(dto.getPlace())          // "이름|위도|경도|placeId" 그대로 저장
                .regdate(Timestamp.valueOf(LocalDateTime.now()))
                .readCount(0)
                .favoriteCount(0)
                .contentState(state)
                .state("ACTIVE")
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
    
    @Transactional
    public void edit(BoardReviewDTO dto) {

    	BoardReview review = boardReviewRepository
    	    .findById(dto.getSeqReview())
    	    .orElseThrow(() -> new RuntimeException("글 없음"));

    	// ✅ 작성자 체크 (username 기준)
    	String writerUserId = review.getUser().getUserId();   // DB에 저장된 작성자
    	String loginUserId  = dto.getUserId();                // 로그인한 사용자

    	if (!writerUserId.equals(loginUserId)) {
    	    throw new RuntimeException("권한 없음");
    	}

        review.setTitle(dto.getTitle());
        review.setReviewContent(dto.getReviewContent());
        review.setStarRating(dto.getStarRating());
        review.setPlace(dto.getPlace());
        review.setContentState(dto.getContentState());

        try {
            if (dto.getFile() != null && !dto.getFile().isEmpty()) {
                // 🔥 새 이미지 → S3 업로드
                String imageUrl = s3Uploader.upload(dto.getFile(), "review");
                review.setAttach(imageUrl);
            } else {
                // 🔥 새 파일 없으면 기존 이미지 유지
                review.setAttach(dto.getExistingAttach());
            }
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        // 태그는 기존 삭제 후 재삽입
        taggingRepository.deleteByReview(review);
        // add() 때 쓰던 태그 저장 로직 재사용
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
                                .review(review)
                                .hashtag(hashtag)
                                .build()
                );
            }
        }
    }
    
    @Override
    public BoardReviewDTO getReviewForEdit(Long seqReview) {

        BoardReview review = boardReviewRepository.findById(seqReview)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

        BoardReviewDTO dto = review.toDTO();

        // 태그도 수정 화면에 필요하면 같이 넣기
        dto.setTagList(
            taggingRepository.findByReview(review)
                .stream()
                .map(t -> t.getHashtag().getHashtag())
                .toList()
        );

        return dto;
    }
    
    @Override
    @Transactional
    public void deleteReview(Long seqReview, String username) {

        BoardReview review = boardReviewRepository.findById(seqReview)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

        // 작성자 체크
        if (!review.getUser().getUserId().equals(username)) {
            throw new RuntimeException("권한 없음");
        }

        // 🔥 삭제 처리 (soft delete)
        review.setState("DELETED");
        review.setTitle("삭제된 리뷰입니다");
        review.setReviewContent("삭제된 리뷰입니다");
        review.setAttach(null);
        review.setPlace(null);
        review.setStarRating(null);
        
        if (review.getContentState() == null) {
            review.setContentState("일반글");
        }

        // 태그 전부 제거
        taggingRepository.deleteByReview(review);

        // 좋아요 / 스크랩 제거
        favoriteReviewRepository.deleteByReview(review);
        scrapReviewRepository.deleteByReview(review);
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
            
            if ("Y".equals(c.getState())) {
                dto.setNickname(null);
                dto.setRegdate(null);
                dto.setContent("삭제된 댓글입니다.");
                dto.setState("Y");
            }

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

    	User user = userRepository.findByUserId(dto.getUserId())
    	        .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    	
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
                .state("ACTIVE")
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
    @Transactional
    public boolean toggleFavorite(Long seqReview, String username) {

        Optional<FavoriteReview> existing = favoriteReviewRepository
                .findByUser_UserIdAndReviewSeqReview(username, seqReview);

        if (existing.isPresent()) {
            favoriteReviewRepository.delete(existing.get());
            return false; // 취소됨
        } else {
            FavoriteReview fav = FavoriteReview.builder()
                    .user(userRepository.findByUserId(username).orElseThrow())
                    .review(boardReviewRepository.findById(seqReview).orElseThrow())
                    .regdate(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            favoriteReviewRepository.save(fav);
            return true; // 좋아요 추가됨
        }
    }
    
    @Override
    @Transactional
    public boolean toggleScrap(Long seqReview, String username) {

        Optional<ScrapReview> existing = scrapReviewRepository
                .findByUser_UserIdAndReviewSeqReview(username, seqReview);

        if (existing.isPresent()) {
            scrapReviewRepository.delete(existing.get());
            return false;
        } else {
            ScrapReview scrap = ScrapReview.builder()
                    .user(userRepository.findByUserId(username).orElseThrow())
                    .review(boardReviewRepository.findById(seqReview).orElseThrow())
                    .regdate(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            scrapReviewRepository.save(scrap);
            return true;
        }
    }
    
    @Override
    public boolean isFavorite(Long seqReview, String username) {
        return favoriteReviewRepository
                .existsByUserUserIdAndReviewSeqReview(username, seqReview);
    }

    @Override
    public boolean isScrap(Long seqReview, String username) {
        return scrapReviewRepository
                .existsByUser_UserIdAndReviewSeqReview(username, seqReview);
    }
    
    @Override
    public int getFavoriteCount(Long seqReview) {
        return favoriteReviewRepository.countByReviewSeqReview(seqReview);
    }
    
    @Override
    public int getCommentCount(Long seqReview) {
        return commentRepository.countByReviewSeqReviewAndState(seqReview, "ACTIVE");
    }
    
    @Override
    public CommentDTO editComment(Long seqComment, String username, String content) {

        Comment entity = commentRepository.findById(seqComment).orElseThrow();

        // 본인 댓글인지 체크(선택)
        if (!entity.getUser().getUserId().equals(username)) {
            throw new RuntimeException("권한 없음");
        }

        entity.setContent(content);
        entity.setRegdate(Timestamp.valueOf(LocalDateTime.now()));

        commentRepository.save(entity);

        // DTO 변환
        CommentDTO dto = new CommentDTO();
        dto.setSeqComment(entity.getSeqComment());
        dto.setUserId(entity.getUser().getUserId());
        dto.setNickname(entity.getUser().getNickname());
        dto.setContent(entity.getContent());
        dto.setRegdate(entity.getRegdate());

        return dto;
    }
    
    @Override
    public boolean deleteComment(Long seqComment, String username) {
        Comment comment = commentRepository.findById(seqComment).orElseThrow();

        if (!comment.getUser().getUserId().equals(username)) {
            return false;
        }

        comment.setState("DELETED");
        comment.setContent("삭제된 댓글입니다.");
        commentRepository.save(comment);

        return true;
    }
	
    @Override
    public List<BoardReviewDTO> getMyReviews(String username) {
        return boardReviewRepository.findByUser_UserIdAndStateOrderByRegdateDesc(username, "ACTIVE")
                .stream()
                .map(BoardReview::toDTO)
                .toList();
    }

    @Override
    public List<CommentDTO> getMyComments(String username) {
        return commentRepository
                .findByUser_UserIdAndStateOrderByRegdateDesc(username, "ACTIVE")
                .stream()
                .map(Comment::toDTO)
                .toList();
    }

    @Override
    public List<BoardReviewDTO> getMyFavorites(String username) {

        return favoriteReviewRepository
                .findByUser_UserIdAndReview_StateOrderByRegdateDesc(
                		username, "ACTIVE"
                )
                .stream()
                .map(fr -> fr.getReview().toDTO())
                .toList();
    }

	@Override
	public List<BoardReviewDTO> getMyScraps(String username) {
		
		return scrapReviewRepository
                .findByUser_UserIdAndReview_StateOrderByRegdateDesc(
                		username, "ACTIVE"
                )
                .stream()
                .map(fr -> fr.getReview().toDTO())
                .toList();
	}

	@Override
	public BoardReviewDTO getReview(Long seqReview) {

	    // 1. 리뷰 엔티티 조회
	    BoardReview review = boardReviewRepository.findById(seqReview)
	            .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));
	    
	    if ("DELETED".equals(review.getState())) {
	        throw new IllegalStateException("삭제된 리뷰");
	    }

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
	    
	    int favoriteCnt = favoriteReviewRepository.countByReviewSeqReview(seqReview);
	    dto.setFavoriteCount(favoriteCnt);

	    return dto;
	}
	

}
