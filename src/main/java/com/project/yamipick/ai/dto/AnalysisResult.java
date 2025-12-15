package com.project.yamipick.ai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    private List<String> positive;
    private List<String> negative;
    private String emotion;

}
