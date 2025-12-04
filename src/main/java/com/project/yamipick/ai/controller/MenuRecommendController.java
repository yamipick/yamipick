package com.project.yamipick.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;
import com.project.yamipick.ai.service.MenuRecommendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MenuRecommendController {

	private final MenuRecommendService menuRecommendService;

    @PostMapping("/api/recommend/menu")
    public List<MenuRecommendResponse> searchMenuRecommendations(@RequestBody MenuRecommendRequest request) {
        return menuRecommendService.recommend(request);
    }

}

