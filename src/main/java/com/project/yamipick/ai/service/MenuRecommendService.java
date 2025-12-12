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
        List<String> negativeTags = req.getNegativeTags();
        List<Long> excludeMenuIds = req.getExcludeMenuIds();
        
        if (selectedTags == null || selectedTags.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 1. 전체 메뉴 로드
        List<Menu> menus = menuRepository.findAll();

        return menus.stream()
        		//최근 추천 메뉴 제외
        		.filter(menu -> {
        			if (excludeMenuIds == null || excludeMenuIds.isEmpty()) {
        				return true;
        			}
        			return !excludeMenuIds.contains(menu.getSeqMenu());
        		})
                .map(menu -> {

                    List<String> menuTags = parseTags(menu.getFlavorTags());
                    int score = 0;

                    //positive + 10
                    if (selectedTags != null) {
                    	for (String tag : selectedTags) {
                    		if (menuTags.contains(tag)) {
                    			score += 10;
                    		}
                    	}
                    }
                    
                    //negative -999
                    if (negativeTags != null) {
                    	for (String tag : negativeTags) {
                    		if (menuTags.contains(tag)) {
                    			score = -999;
                    		}
                    	}
                    }

                    //이미지 (동기 아님. block() 제거!)
                    String menuImage = pexelsService
                            .getFoodImageSync(menu.getMenuEnglish());
                    if (menuImage == null || menuImage.isBlank()) {
                    	menuImage = "/img/menu/default.jpg"; 
                    }
                    
                    //positive와 메뉴 태그의 교집합 (UI 표시용)
                    List<String> matched = getMatchedTags(menuTags, selectedTags);
                    
                    return MenuRecommendResponse.builder()
                            .seqMenu(menu.getSeqMenu())
                            .menuName(menu.getMenuName())
                            .menuDescription(menu.getMenuDescription())
                            .menuImage(menuImage)
                            .allTags(menuTags)
                            .matchedTags(matched)
                            .score(score)
                            .reason(null)
                            .build();
                })
                //점수 높은 순으로 정렬
                .sorted((a,b) -> Integer.compare(b.getScore(), a.getScore()))
                //점수 0 이하 제외
                .filter(r -> r.getScore() > 0)
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
    
    private List<String> getMatchedTags(List<String> menuTags, List<String> positive) {
    	if (positive == null) return List.of();
    	return positive.stream()
    			.filter(menuTags::contains)
    			.collect(Collectors.toList());
    }
}
