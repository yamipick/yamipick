package com.project.yamipick.ai.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedTags {
	
	@JsonProperty("positive_tags")
    private List<String> positiveTags;

    @JsonProperty("negative_tags")
    private List<String> negativeTags;

    // NPE 방지용 헬퍼
    public List<String> safePositive() {
        return positiveTags != null ? positiveTags : new ArrayList<>();
    }

    public List<String> safeNegative() {
        return negativeTags != null ? negativeTags : new ArrayList<>();
    }

}
