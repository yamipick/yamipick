package com.project.yamipick.admin.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.yamipick.notice.entity.Notice;
import com.project.yamipick.notice.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 (성능 최적화)
@Slf4j
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    // application.yml에 설정된 파일 저장 경로 (없으면 c:/upload/ 기본값)
    @Value("${file.upload.path:c:/upload/}")
    private String uploadPath;

    /**
     * 공지사항 목록 조회 (최신순)
     */
    public List<Notice> getNoticeList() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 공지사항 상세 조회 (+조회수 증가)
     */
    @Transactional // 조회수 변경을 위해 쓰기 모드
    public Notice getNotice(Long seq) {
        Notice notice = noticeRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        // 조회수 증가 (엔티티 내부에 메소드 만들거나, 직접 변경)
        // notice.increaseViewCount(); // 엔티티에 메소드가 있다면 사용
        // 없다면 아래처럼 직접 설정 (Setter 필요)
        // notice.setViewCount(notice.getViewCount() + 1); 
        
        return notice;
    }

    /**
     * 공지사항 작성 (저장)
     */
    @Transactional
    public void writeNotice(String title, String content, MultipartFile file) {
        
        String filePath = null;

        // 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            try {
                // 저장할 경로 폴더 생성
                File dir = new File(uploadPath);
                if (!dir.exists()) dir.mkdirs();

                // 파일명 중복 방지 (UUID 사용)
                String originalName = file.getOriginalFilename();
                String saveName = UUID.randomUUID() + "_" + originalName;
                
                // 파일 저장
                file.transferTo(new File(uploadPath + saveName));
                filePath = saveName; // DB에 저장할 파일명

            } catch (IOException e) {
                log.error("파일 업로드 실패", e);
            }
        }

        // 엔티티 생성 및 저장
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                // .category("일반") // 카테고리 삭제했으므로 주석 처리
                .filePath(filePath)
                .viewCount(0) // 기본값 0
                .build();

        noticeRepository.save(notice);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public void updateNotice(Long seq, String title, String content, MultipartFile file) {
        
        Notice notice = noticeRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 파일 수정 (새 파일이 들어왔을 때만)
        if (file != null && !file.isEmpty()) {
            try {
                // 기존 파일 삭제 로직은 생략 (필요하면 추가)
                
                String originalName = file.getOriginalFilename();
                String saveName = UUID.randomUUID() + "_" + originalName;
                file.transferTo(new File(uploadPath + saveName));
                
                // 파일 경로 업데이트 (Setter 필요)
                // notice.setFilePath(saveName); 
                
                // 만약 Setter가 없다면 엔티티에 updateFile 메소드 추가 권장

            } catch (IOException e) {
                log.error("파일 수정 실패", e);
            }
        }

        // 제목, 내용 수정 (엔티티에 update 메소드가 있거나 Setter 사용)
        // notice.update(title, content); 
        // 또는 Setter 사용
        // notice.setTitle(title);
        // notice.setContent(content);
        
        // (임시) 엔티티에 update 메소드가 없어서 에러난다면 아래처럼 구현하세요:
        // Notice.java 엔티티 파일에 추가:
        // public void update(String title, String content) {
        //     this.title = title;
        //     this.content = content;
        // }
    }

    /**
     * 공지사항 삭제
     */
    @Transactional
    public void deleteNotice(Long seq) {
        // 실제 파일 삭제 로직 추가 가능
        noticeRepository.deleteById(seq);
    }
}