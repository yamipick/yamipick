package com.project.yamipick.ai.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.AIRecommendDTO;
import com.project.yamipick.ai.dto.ExtractedTags;
import com.project.yamipick.ai.dto.MenuRecommendRequest;
import com.project.yamipick.ai.dto.MenuRecommendResponse;
import com.project.yamipick.ai.entity.Menu;
import com.project.yamipick.ai.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuRecommendService {

    private final MenuRepository menuRepository;
    private final AIRecommendService aiRecommendService;
    private final AIService aiService;

    public List<MenuRecommendResponse> recommend(MenuRecommendRequest req) {

    	// 1. 태그 로딩
        List<String> positiveTags = req.getPositiveTags() != null
                ? new ArrayList<>(req.getPositiveTags())
                : new ArrayList<>();

        List<String> negativeTags = req.getNegativeTags() != null
                ? req.getNegativeTags()
                : List.of();

        List<String> contextTags = req.getContextTags() != null
                ? req.getContextTags()
                : List.of();

        // 감정 기반 태그 무조건 합치기
        if (req.getEmotion() != null) {
            List<String> emotionTags = aiService.emotionToTags(req.getEmotion());
            for (String e : emotionTags) {
                if (!positiveTags.contains(e)) {
                    positiveTags.add(e); // 중복 방지
                }
            }
        }
        
        // 태그가 최종적으로 0개 → 완전 랜덤 추천
        if (positiveTags.isEmpty()) {
            return recommendRandom(req, negativeTags, contextTags);
        }

        // 2. 메뉴 전체 로드
        List<Menu> menus = menuRepository.findAll();

        // 3. 태그 기반 스코어 계산
        List<MenuRecommendResponse> result = menus.stream()
                .map(menu -> {

                	List<String> menuTags = menu.getFlavorTags() != null
                            ? Arrays.stream(menu.getFlavorTags().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toList()
                            : List.of();

                    // 3-1. negative 태그 포함 메뉴는 제외
                    for (String neg : negativeTags) {
                        if (menuTags.contains(neg)) {
                            return null;
                        }
                    }

                    // 3-2. positive 매칭
                    List<String> matchedPositive = positiveTags.stream()
                            .filter(menuTags::contains)
                            .collect(Collectors.toList());

                    int score = matchedPositive.size();

                    // 3-3. context 태그 보너스
                    for (String ctx : contextTags) {
                        if (menuTags.contains(ctx)) {
                            score += 1;
                        }
                    }
                    
                    //감정 가중치
                    String emotion = req.getEmotion();

                    if ("sick".equals(emotion)) {
                        if (menuTags.contains("soup")) score += 2;
                        if (menuTags.contains("healthy")) score += 2;
                    }
                    if ("stressed".equals(emotion)) {
                        if (menuTags.contains("spicy")) score += 2;
                    }
                    if ("tired".equals(emotion)) {
                        if (menuTags.contains("healthy")) score += 1;
                    }

                    if (score == 0) return null;

                    return MenuRecommendResponse.builder()
                            .seqMenu(menu.getSeqMenu())
                            .menuName(menu.getMenuName())
                            .menuImage(menu.getMenuImage())
                            .menuDescription(menu.getMenuDescription())
                            .matchedTags(matchedPositive)
                            .score(score)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getScore() - a.getScore())
                .limit(3)
                .collect(Collectors.toList());
        
        // 태그로 걸리는 메뉴가 하나도 없으면 → 랜덤 Fallback
        if (result.isEmpty()) {
            return recommendRandom(req, negativeTags, contextTags);
        }

        // 4. BEST 메뉴 1개
        MenuRecommendResponse best = result.get(0);

        // 5. 추천 이유 생성 (감정 포함)
        ExtractedTags tagsObj = new ExtractedTags(positiveTags, negativeTags, contextTags);

        String reason = aiService.generateReason(
                req.getUserInput(),
                best,
                tagsObj,
                req.getEmotion()
        );
        best.setReason(reason);

        // 6. BEST 1개만 tblAIRecommend 저장
        aiRecommendService.saveRecommend(
                AIRecommendDTO.builder()
                        .seqMenu(best.getSeqMenu())
                        .userInput(req.getUserInput())
                        .aiReason(reason)
                        .seqSession(req.getSeqSession())
                        .build()
        );

        // 7. TOP3 반환 (화면 표시용)
        return result;
    }
    
    /** 태그가 정말 하나도 없을 때 쓰는 랜덤 추천 + DB 저장 */
    private List<MenuRecommendResponse> recommendRandom(MenuRecommendRequest req,
                                                        List<String> negativeTags,
                                                        List<String> contextTags) {

        List<Menu> menus = menuRepository.findAll();
        if (menus.isEmpty()) return List.of();

        // negative 태그는 그래도 제외
        List<Menu> candidates = menus.stream()
                .filter(menu -> {
                    if (menu.getFlavorTags() == null) return true;
                    List<String> menuTags = Arrays.stream(menu.getFlavorTags().split(","))
                            .map(String::trim)
                            .toList();
                    for (String neg : negativeTags) {
                        if (menuTags.contains(neg)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        if (candidates.isEmpty()) return List.of();

        Collections.shuffle(candidates);

        List<MenuRecommendResponse> result = candidates.stream()
                .limit(3)
                .map(m -> MenuRecommendResponse.builder()
                        .seqMenu(m.getSeqMenu())
                        .menuName(m.getMenuName())
                        .menuImage(m.getMenuImage())
                        .menuDescription(m.getMenuDescription())
                        .matchedTags(List.of()) // 랜덤이라 매칭 태그는 없음
                        .score(1)
                        .build())
                .collect(Collectors.toList());

        // BEST = 첫 번째
        MenuRecommendResponse best = result.get(0);

        ExtractedTags tagsObj = new ExtractedTags(
                List.of(), negativeTags, contextTags
        );

        String reason = aiService.generateReason(
                req.getUserInput(),
                best,
                tagsObj,
                req.getEmotion()
        );
        best.setReason(reason);

        aiRecommendService.saveRecommend(
                AIRecommendDTO.builder()
                        .seqMenu(best.getSeqMenu())
                        .userInput(req.getUserInput())
                        .aiReason(reason)
                        .seqSession(req.getSeqSession())
                        .build()
        );

        return result;
    }
}
