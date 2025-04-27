package com.example.tedtalksanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TedTalkAnalyticsDTO {
    private Map<String, Double> speakerInfluence;
    private Map<Integer, String> bestTalkPerYear;
    private Map<Integer, Long> talksPerYear;
}
