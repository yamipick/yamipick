package com.project.yamipick.ai.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;
import com.project.yamipick.ai.entity.Menu;
import com.project.yamipick.ai.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuRecommendService {

    private final MenuRepository menuRepository;
    private final PexelsService pexelsService;

    public List<MenuRecommendResponse> recommend(MenuRecommendRequest req) {

        List<String> selectedTags = req.getPositiveTags();
        if (selectedTags == null || selectedTags.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 전체 메뉴 로드
        List<Menu> menus = menuRepository.findAll();

        // 2. 각 메뉴의 flavorTags 를 파싱해서, 선택된 태그를 모두 포함하는 메뉴만 필터
        return menus.stream()
                // 1) 태그 필터 먼저 적용
                .filter(menu -> {
                    List<String> menuTags = parseTags(menu.getFlavorTags());
                    return selectedTags.stream().allMatch(menuTags::contains);
                })
                // 2) 매핑 단계
                .map(menu -> {

                    List<String> menuTags = parseTags(menu.getFlavorTags());

                    // 3) 이미지 가져오기 (동기 아님. block() 제거!)
                    String menuImage = pexelsService
                            .getFoodImageSync(menu.getMenuEnglish());
                    if (menuImage == null || menuImage.isBlank()) {
                    	menuImage = "/img/menu/default.jpa"; 
                    }

                    return MenuRecommendResponse.builder()
                            .seqMenu(menu.getSeqMenu())
                            .menuName(menu.getMenuName())
                            .menuDescription(menu.getMenuDescription())
                            .menuImage(menuImage)
                            .matchedTags(selectedTags)
                            .allTags(menuTags)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** "spicy,noodle,oily" 이런 CSV 문자열을 List<String> 으로 파싱 */
    private List<String> parseTags(String flavorTags) {
        if (flavorTags == null || flavorTags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(flavorTags.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
