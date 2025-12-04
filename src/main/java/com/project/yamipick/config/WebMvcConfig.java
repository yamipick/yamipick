package com.project.yamipick.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:C:/yamipick/upload/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /upload/** 로 시작하는 URL 요청은 로컬 디스크의 uploadPath 폴더에서 파일을 찾는다.
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + uploadPath);
    }
}