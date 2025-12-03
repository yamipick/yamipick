package com.project.yamipick.ai.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.yamipick.ai.dto.MenuRecommendResponse;
import com.project.yamipick.ai.dto.TagRequest;
import com.project.yamipick.ai.service.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MenuRecommendController {

    private final MenuService menuService;

    @PostMapping("/api/recommend/menu")
    public List<MenuRecommendResponse> recommendMenus(@RequestBody TagRequest request) {
        return menuService.recommendMenusByTags(request.getTags());
    }
}

