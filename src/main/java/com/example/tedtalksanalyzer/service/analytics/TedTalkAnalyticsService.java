package com.example.tedtalksanalyzer.service.analytics;

import static com.example.tedtalksanalyzer.cache.CacheConstants.BEST_TALK_PER_YEAR;
import static com.example.tedtalksanalyzer.cache.CacheConstants.TALKS_PER_YEAR;
import static com.example.tedtalksanalyzer.cache.CacheConstants.TOP_SPEAKER_TOPIC;

import com.example.tedtalksanalyzer.cache.RedisService;
import com.example.tedtalksanalyzer.dto.TedTalkAnalyticsDTO;
import com.example.tedtalksanalyzer.dto.TedTalkDTO;
import com.example.tedtalksanalyzer.dto.TedTalkInfluenceDTO;
import com.example.tedtalksanalyzer.exception.AnalyticsCacheException;
import com.example.tedtalksanalyzer.exception.TedTalkImportException;
import com.example.tedtalksanalyzer.model.TedTalk;
import com.example.tedtalksanalyzer.service.TedTalkDataService;
import com.example.tedtalksanalyzer.service.utils.TedTalkMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TedTalkAnalyticsService {

    private final StringRedisTemplate redisTemplate;
    private final RedisService redisService;
    private final TedTalkDataService tedTalkDataService;
    private final ObjectMapper objectMapper;

    public void recalculateAnalyticsFromDatabase() {
        int page = 0;
        List<TedTalk> allTedTalks = new ArrayList<>();

        while (true) {
            List<TedTalk> batch = tedTalkDataService.getTedTalksPage(page);
            if (batch.isEmpty()) {
                break;
            }
            allTedTalks.addAll(batch);
            page++;
        }

        if (!allTedTalks.isEmpty()) {
            calculateAndStoreAnalytics(allTedTalks);
            log.info("Analytics recalculated and cache updated from database. Total talks: {}", allTedTalks.size());
        } else {
            log.warn("No TedTalks found in database to recalculate analytics.");
        }
    }


    public TedTalkAnalyticsDTO calculateAndStoreAnalytics(List<TedTalk> tedTalks) {
        TedTalkAnalyticsDTO analytics = calculateAnalytics(tedTalks);
        try {
            redisService.saveTopSpeakers(analytics.getSpeakerInfluence());
            redisService.saveBestTalksPerYear(analytics.getBestTalkPerYear());
            redisService.saveAmountTalksPerYear(analytics.getTalksPerYear());
            redisService.updateAnalyticsLastUpdated();
            log.info("Analytics cache updated. Total talks analyzed: {}", tedTalks.size());
        } catch (Exception e) {
            log.error("Failed to update analytics cache", e);
            throw new AnalyticsCacheException("Failed to update analytics cache", e);
        }
        return analytics;
    }

    private TedTalkAnalyticsDTO calculateAnalytics(List<TedTalk> tedTalks) {
        List<TedTalkDTO> dtos = tedTalks.stream()
                .map(TedTalkMapper::toResponseDTO)
                .toList();

        Map<String, Double> speakerInfluence = new HashMap<>();
        Map<Integer, TedTalkDTO> bestTalkPerYear = new HashMap<>();
        Map<Integer, Long> talksPerYear = new HashMap<>();

        for (TedTalkDTO dto : dtos) {
            String author = dto.getAuthor();
            int year = dto.getDate().getYear();
            long views = dto.getViews();
            long likes = dto.getLikes();

            double engagementRate = (double) likes / Math.max(views, 1);
            double score = Math.sqrt(views) * engagementRate;

            speakerInfluence.merge(author, score, Double::sum);

            bestTalkPerYear.merge(year, dto, (existing, current) -> {
                double existingEngagement = (double) existing.getLikes() / Math.max(existing.getViews(), 1);
                double currentEngagement = (double) current.getLikes() / Math.max(current.getViews(), 1);

                double existingScore = Math.sqrt(existing.getViews()) * existingEngagement;
                double currentScore = Math.sqrt(current.getViews()) * currentEngagement;

                return currentScore > existingScore ? current : existing;
            });

            talksPerYear.merge(year, 1L, Long::sum);
        }

        Map<Integer, String> bestTalkPerYearJson = bestTalkPerYear.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> serializeTalkToJson(entry.getValue())
                ));

        return new TedTalkAnalyticsDTO(speakerInfluence, bestTalkPerYearJson, talksPerYear);
    }

    private String serializeTalkToJson(TedTalkDTO tedTalkDTO) {
        try {
            return objectMapper.writeValueAsString(tedTalkDTO);
        } catch (JsonProcessingException e) {
            throw new TedTalkImportException("Failed to serialize TedTalk to JSON", e);
        }
    }

    public List<TedTalkInfluenceDTO> getTopSpeakers(int count) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> topSpeakers = zSetOps.reverseRangeWithScores(TOP_SPEAKER_TOPIC, 0, count - 1L);

        if (CollectionUtils.isEmpty(topSpeakers)) {
            return Collections.emptyList();
        }

        return topSpeakers.stream()
                .map(tuple -> new TedTalkInfluenceDTO(tuple.getValue(), tuple.getScore().longValue()))
                .collect(Collectors.toList());
    }

    public Long getAmountOfTalksPerYear(int year) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        var value = hashOps.get(TALKS_PER_YEAR, String.valueOf(year));

        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.error("Failed to parse talks per year data for year {}", year, e);
            return 0L;
        }
    }

    public TedTalkDTO getBestTalkByYear(int year) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        var json = hashOps.get(BEST_TALK_PER_YEAR, String.valueOf(year));

        try {
            return objectMapper.readValue(json.toString(), TedTalkDTO.class);
        } catch (Exception e) {
            log.error("Failed to parse best talk JSON", e);
            throw new AnalyticsCacheException("Failed to parse best talk JSON");
        }
    }
}
