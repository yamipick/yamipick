package com.project.yamipick.ai.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.MenuDTO;
import com.project.yamipick.ai.dto.MenuRecommendResponse;
import com.project.yamipick.ai.entity.Menu;
import com.project.yamipick.ai.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<String> getAllMenuNames() {
        return menuRepository.findAll().stream()
                .map(Menu::getMenuName)
                .collect(Collectors.toList());
    }
    
    public List<MenuDTO> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(menu -> MenuDTO.builder()
                        .seqMenu(menu.getSeqMenu())
                        .menuName(menu.getMenuName())
                        .menuCategory(menu.getMenuCategory())
                        .flavorTags(menu.getFlavorTags())
                        .menuDescription(menu.getMenuDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public MenuDTO getMenu(Long seqMenu) {

        Menu menu = menuRepository.findById(seqMenu)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));

        return MenuDTO.builder()
                .seqMenu(menu.getSeqMenu())
                .menuName(menu.getMenuName())
                .menuCategory(menu.getMenuCategory())
                .flavorTags(menu.getFlavorTags())
                .menuDescription(menu.getMenuDescription())
                .build();
    }
    
    public List<MenuDTO> getMenusByNames(List<String> names) {
        return menuRepository.findByMenuNameIn(names).stream()
                .map(menu -> MenuDTO.builder()
                        .seqMenu(menu.getSeqMenu())
                        .menuName(menu.getMenuName())
                        .menuCategory(menu.getMenuCategory())
                        .flavorTags(menu.getFlavorTags())
                        .menuDescription(menu.getMenuDescription())
                        .build()
                )
                .toList();
    }

    public List<MenuRecommendResponse> recommendMenusByTags(List<String> userTags) {

        List<Menu> menus = menuRepository.findAll();

        return menus.stream()
                .map(menu -> {

                    // 메뉴 태그 파싱
                    List<String> menuTags = Arrays.asList(menu.getFlavorTags().split(","));

                    // 사용자 태그와 겹치는 태그 찾기
                    List<String> matched = menuTags.stream()
                        .filter(userTags::contains)
                        .collect(Collectors.toList());

                    int score = matched.size(); // 겹친 개수

                    return MenuRecommendResponse.builder()
                            .seqMenu(menu.getSeqMenu())
                            .menuName(menu.getMenuName())
                            .matchedTags(matched)
                            .score(score)
                            .build();
                })
                .filter(r -> r.getScore() > 0) // 하나도 안 겹치면 제외
                .sorted(Comparator.comparing(MenuRecommendResponse::getScore).reversed())
                .collect(Collectors.toList());
    }

    
}
