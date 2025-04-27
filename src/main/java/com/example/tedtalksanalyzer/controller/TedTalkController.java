package com.example.tedtalksanalyzer.controller;

import com.example.tedtalksanalyzer.dto.TedTalkDTO;
import com.example.tedtalksanalyzer.service.TedTalkDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tedtalks")
@RequiredArgsConstructor
public class TedTalkController {

    private final TedTalkDataService tedTalkDataService;

    @PostMapping
    public ResponseEntity<TedTalkDTO> createTedTalk(@RequestBody TedTalkDTO tedTalkDto) {
        tedTalkDataService.createTedTalk(tedTalkDto);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<TedTalkDTO>> getAllTedTalks() {
        return ResponseEntity.ok(tedTalkDataService.getAllTedTalks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TedTalkDTO> getTedTalkById(@PathVariable UUID id) {
        TedTalkDTO tedTalkDTO = tedTalkDataService.getTedTalkById(id);
        return ResponseEntity.ok(tedTalkDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TedTalkDTO> updateTedTalk(@PathVariable UUID id, @RequestBody TedTalkDTO tedTalk) {
        TedTalkDTO updated = tedTalkDataService.updateTedTalk(id, tedTalk);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTedTalk(@PathVariable UUID id) {
        tedTalkDataService.deleteTedTalk(id);
        return ResponseEntity.noContent().build();
    }
}
