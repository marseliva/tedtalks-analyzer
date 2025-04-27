package com.example.tedtalksanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void saveTopSpeakers(Map<String, Long> speakerScores) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        speakerScores.forEach((speaker, score) ->
                zSetOps.add("top_speakers", speaker, score)
        );
    }

    public void saveBestTalksPerYear(Map<Integer, String> yearTalksJson) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        yearTalksJson.forEach((year, talkJson) ->
                hashOps.put("best_talk_per_year", year.toString(), talkJson)
        );
    }

    public void saveAmountTalksPerYear(Map<Integer, Long> yearTalksJson) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        yearTalksJson.forEach((year, count) ->
                hashOps.put("talks_per_year", year.toString(), count.toString())
        );
    }

    public void updateAnalyticsLastUpdated() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        redisTemplate.opsForValue().set("analytics:last_updated", timestamp);
    }
}
