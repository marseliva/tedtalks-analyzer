package com.example.tedtalksanalyzer.controller;

import com.example.tedtalksanalyzer.dto.SearchRequest;
import com.example.tedtalksanalyzer.dto.TedTalkDTO;
import com.example.tedtalksanalyzer.dto.TedTalkInfluenceDTO;
import com.example.tedtalksanalyzer.service.TedTalkService;
import com.example.tedtalksanalyzer.service.analytics.TedTalkAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class TedTalkAnalyticController {

    private final TedTalkAnalyticsService analyticsService;
    private final TedTalkService tedTalkService;

    @GetMapping("/top-speakers")
    public ResponseEntity<List<TedTalkInfluenceDTO>> getTopSpeakers(@RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(analyticsService.getTopSpeakers(count));
    }

    @GetMapping("/best-talk/{year}")
    public ResponseEntity<?> getBestTalkByYear(@PathVariable int year) {
        var bestTalk = analyticsService.getBestTalkByYear(year);
        if (bestTalk == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bestTalk);
    }

    @GetMapping("/talks-count/{year}")
    public ResponseEntity<Long> getAmountOfTalksPerYear(@PathVariable int year) {
        Long count = analyticsService.getAmountOfTalksPerYear(year);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/search")
    public ResponseEntity<List<TedTalkDTO>> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(tedTalkService.search(request));
    }
}
