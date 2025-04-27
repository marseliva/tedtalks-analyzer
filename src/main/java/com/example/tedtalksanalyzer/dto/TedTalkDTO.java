package com.example.tedtalksanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TedTalkDTO {
    private UUID id;
    private String title;
    private String author;
    private LocalDate date;
    private Long views;
    private Long likes;
    private String link;
}
