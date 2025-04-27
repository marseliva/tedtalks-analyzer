package com.example.tedtalksanalyzer.cache;

import static com.example.tedtalksanalyzer.cache.CacheConstants.BEST_TALK_PER_YEAR;
import static com.example.tedtalksanalyzer.cache.CacheConstants.LAST_UPDATED_KEY;
import static com.example.tedtalksanalyzer.cache.CacheConstants.TALKS_PER_YEAR;
import static com.example.tedtalksanalyzer.cache.CacheConstants.TOP_SPEAKER_TOPIC;

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

    public void saveTopSpeakers(Map<String, Double> speakerScores) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        speakerScores.forEach((speaker, score) -> {
            if (score != null && !score.isNaN() && !score.isInfinite()) {
                zSetOps.add(TOP_SPEAKER_TOPIC, speaker, score);
            }
        });
    }

    public void saveBestTalksPerYear(Map<Integer, String> yearTalksJson) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        yearTalksJson.forEach((year, talkJson) ->
                hashOps.put(BEST_TALK_PER_YEAR, year.toString(), talkJson)
        );
    }

    public void saveAmountTalksPerYear(Map<Integer, Long> yearTalksJson) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        yearTalksJson.forEach((year, count) ->
                hashOps.put(TALKS_PER_YEAR, year.toString(), count.toString())
        );
    }

    public void updateAnalyticsLastUpdated() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        redisTemplate.opsForValue().set(LAST_UPDATED_KEY, timestamp);
    }
}
