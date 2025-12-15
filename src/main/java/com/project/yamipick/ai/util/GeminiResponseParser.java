package com.project.yamipick.ai.util;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GeminiResponseParser {

	private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> extractMenuNames(String geminiResponse) {

        try {
            var tree = mapper.readTree(geminiResponse);

            String text = tree
                    .get("candidates")
                    .get(0)
                    .get("content")
                    .get("parts")
                    .get(0)
                    .get("text")
                    .asText();

            // text → ["김치찌개","짬뽕"] 같은 JSON 배열 문자열
            return mapper.readValue(text, List.class);

        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 오류", e);
        }
    }
	
}
