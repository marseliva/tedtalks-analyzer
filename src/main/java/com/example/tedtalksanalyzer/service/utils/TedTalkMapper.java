package com.example.tedtalksanalyzer.service.utils;

import com.example.tedtalksanalyzer.dto.TedTalkDTO;
import com.example.tedtalksanalyzer.model.TedTalk;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TedTalkMapper {

    public static TedTalk toEntity(TedTalkDTO dto) {
        TedTalk tedTalk = new TedTalk();
        tedTalk.setTitle(dto.getTitle());
        tedTalk.setAuthor(dto.getAuthor());
        tedTalk.setDate(dto.getDate());
        tedTalk.setViews(dto.getViews());
        tedTalk.setLikes(dto.getLikes());
        tedTalk.setLink(dto.getLink());
        return tedTalk;
    }

    public static TedTalkDTO toResponseDTO(TedTalk entity) {
        return TedTalkDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .date(entity.getDate())
                .views(entity.getViews())
                .likes(entity.getLikes())
                .link(entity.getLink())
                .build();
    }
}
