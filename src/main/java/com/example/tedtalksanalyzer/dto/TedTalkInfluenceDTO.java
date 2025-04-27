package com.example.tedtalksanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TedTalkInfluenceDTO {
    private String author;
    private Long score;
}
