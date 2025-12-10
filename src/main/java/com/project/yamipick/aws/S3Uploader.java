package com.project.yamipick.aws;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Uploader {

    // ★ 최신 라이브러리(3.x)의 핵심 도구 (알아서 AWS랑 연결됨)
    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 메서드 (공용)
     * @param file : 업로드할 파일 (이미지 등)
     * @param dirName : S3 내부 폴더 이름 (예: "banner", "profile")
     * @return : 업로드된 이미지의 URL (https://...)
     */
    public String upload(MultipartFile file, String dirName) throws IOException {
        
        // 1. 파일 이름 중복 방지 (UUID 사용)
        String originalFileName = file.getOriginalFilename();
        String key = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

        // 2. 파일 데이터 읽기
        InputStream inputStream = file.getInputStream();

        // 3. AWS S3로 전송 (딱 한 줄로 끝!)
        s3Template.upload(bucket, key, inputStream);

        // 4. 업로드된 이미지의 인터넷 주소(URL)를 받아와서 리턴
        String uploadedUrl = s3Template.download(bucket, key).getURL().toString();
        
        log.info("S3 Upload Success: {}", uploadedUrl);
        return uploadedUrl;
    }
}