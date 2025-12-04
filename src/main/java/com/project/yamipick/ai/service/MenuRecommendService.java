package com.project.yamipick.ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.yamipick.ai.dto.AIRecommendDTO;
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
    private final GeminiService geminiService;


    public List<MenuRecommendResponse> recommend(MenuRecommendRequest req) {

        // === [1] 태그 생성 (챗봇 추천의 경우 tags가 없으므로 userInput에서 태그 파싱 필요) ===
        List<String> effectiveTags;

        if (req.getTags() != null && !req.getTags().isEmpty()) {
            // 태그 기반 추천
            effectiveTags = req.getTags();
        } else {
            // 챗봇 기반 추천이라 userInput 기반으로 태그 생성 필요
            // ✔ 여기는 AI 모델을 사용해 userInput → 태그 변환하는 로직으로 변경 가능
            effectiveTags = extractTagsFromUserInput(req.getUserInput());
        }

        // === [2] 메뉴 전체 로드 ===
        List<Menu> menus = menuRepository.findAll();

        // === [3] 태그 기반 스코어 계산 ===
        List<MenuRecommendResponse> result = menus.stream()
                .map(menu -> {

                    List<String> menuTags = List.of(menu.getFlavorTags().split(","));

                    List<String> matched = effectiveTags.stream()
                            .filter(menuTags::contains)
                            .collect(Collectors.toList());

                    return MenuRecommendResponse.builder()
                            .seqMenu(menu.getSeqMenu())
                            .menuName(menu.getMenuName())
                            .menuImage(menu.getMenuImage())
                            .menuDescription(menu.getMenuDescription())
                            .matchedTags(matched)
                            .score(matched.size())
                            .build();
                })
                .filter(r -> r.getScore() > 0)
                .sorted((a, b) -> b.getScore() - a.getScore())
                .limit(3)
                .collect(Collectors.toList());

        // 추천이 없으면 저장도 안 함
        if (result.isEmpty()) return result;

        // === [4] best 메뉴 1개 선택 ===
        MenuRecommendResponse best = result.get(0);

        // === [5] 추천 이유 생성 ===
        String prompt = String.format(
                "너는 음식 추천 전문가야. 사용자가 선택한 태그를 기준으로 메뉴를 추천하는 이유를 부드럽고 자연스럽게 2~3줄로 설명해줘.\n\n" +
                "조건은 다음과 같아:\n" +
                "- 선택한 태그: %s\n" +
                "- 추천 메뉴 이름: %s\n" +
                "- 메뉴 설명: %s\n" +
                "- 매칭된 태그: %s\n\n" +
                "이 추천이 왜 적합한지 사용자가 이해하기 쉽게 설명해줘.",
                effectiveTags,
                best.getMenuName(),
                best.getMenuDescription(),
                best.getMatchedTags()
        );

        // AI에게 explanation 요청
        String reason = geminiService.generateText(prompt).block();
        best.setReason(reason);
        
        // === [6] 추천 저장 (태그 기반도, 챗봇 기반도 둘 다 저장) ===
        aiRecommendService.saveRecommend(
                AIRecommendDTO.builder()
                        .seqMenu(best.getSeqMenu())
                        .userInput(
                                req.getUserInput() != null ? req.getUserInput() 
                                : String.join(",", effectiveTags)
                        )
                        .aiReason(reason)
                        .seqSession(req.getSeqSession())   // 챗봇 추천이면 값 있음, 태그 추천이면 null
                        .build()
        );

        return result;
    }

    // === 간단한 태그 추출 메서드 (임시, 나중에 AI로 변경 가능) ===
    private List<String> extractTagsFromUserInput(String input) {
        // 기본 구현: 문장을 띄어쓰기로 나눠서 태그처럼 사용
        // 나중에 AI 모델에게 태그 분석하게 바꾸면 됨
        if (input == null || input.isBlank()) return List.of();
        return List.of(input.split(" "));
    }
}
