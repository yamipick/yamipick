package com.project.yamipick.notice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.notice.entity.Notice;
import com.project.yamipick.notice.repository.NoticeRepository;
import com.project.yamipick.user.entity.User;
import com.project.yamipick.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회 전용 (성능 최적화)
public class NoticeService {

	private final NoticeRepository noticeRepository;
	private final UserRepository userRepository;
	
	//공지사항 목록조회 최신순
	public List<Notice> getNoticeList() {
		
		return noticeRepository.findAllByOrderByCreatedAtDesc();
	}
	
	//공지사항 상세조회
	@Transactional // 조회수 증가 때문에 트랜잭션 필요
    public Notice getNotice(Long seqNotice) {
		
        Notice notice = noticeRepository.findById(seqNotice)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지입니다."));
        
        notice.increaseViewCount();
        
        return notice;
    }
	
	//공지사항 등록
	@Transactional
    public Long registerNotice(String title, String content, Long writerSeq, MultipartFile file) {
        
        // (1) 작성자 정보 가져오기
        User writer = userRepository.findById(writerSeq)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));

        // (2) 파일 저장 로직 (일단 비워둠 - 나중에 C-10 구현할 때 채워넣기)
        String filePath = null;
        if (file != null && !file.isEmpty()) {
            // filePath = fileService.upload(file); 
            filePath = "/images/test.jpg"; // 임시 경로
        }
        
     // (3) 엔티티 생성 (Builder 사용)
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .writer(writer) // 객체 연결!
                .filePath(filePath)
                .build();

        // (4) 저장 (JPA 기본 제공 메소드)
        Notice savedNotice = noticeRepository.save(notice);
        
        return savedNotice.getSeqNotice();
    }
	
}
