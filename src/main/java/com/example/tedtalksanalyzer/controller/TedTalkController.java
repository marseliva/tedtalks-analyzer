package com.example.tedtalksanalyzer.controller;

import com.example.tedtalksanalyzer.dto.TedTalkDTO;
import com.example.tedtalksanalyzer.model.TedTalk;
import com.example.tedtalksanalyzer.service.TedTalkService;
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

    private final TedTalkService tedTalkService;

    @PostMapping
    public ResponseEntity<TedTalkDTO> createTedTalk(@RequestBody TedTalk tedTalk) {
        TedTalkDTO tedTalkDTO = tedTalkService.createTedTalk(tedTalk);
        return ResponseEntity.status(201).body(tedTalkDTO);
    }

    @GetMapping
    public ResponseEntity<List<TedTalkDTO>> getAllTedTalks() {
        return ResponseEntity.ok(tedTalkService.getAllTedTalks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TedTalkDTO> getTedTalkById(@PathVariable UUID id) {
        TedTalkDTO tedTalkDTO = tedTalkService.getTedTalkById(id);
        if (tedTalkDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tedTalkDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TedTalkDTO> updateTedTalk(@PathVariable UUID id, @RequestBody TedTalk tedTalk) {
        TedTalkDTO updated = tedTalkService.updateTedTalk(id, tedTalk);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTedTalk(@PathVariable UUID id) {
        tedTalkService.deleteTedTalk(id);
        return ResponseEntity.noContent().build();
    }
}
