package com.example.tedtalksanalyzer.service.analytics;

import static com.example.tedtalksanalyzer.cache.CacheConstants.TALKS_PER_YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.tedtalksanalyzer.cache.RedisService;
import com.example.tedtalksanalyzer.service.TedTalkDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TedTalkAnalyticsServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RedisService redisService;

    @Mock
    private TedTalkDataService tedTalkDataService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TedTalkAnalyticsService tedTalkAnalyticsService;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Test
    void testRecalculateAnalyticsFromDatabaseNoTalks() {
        when(tedTalkDataService.getTedTalksPage(0)).thenReturn(List.of());

        tedTalkAnalyticsService.recalculateAnalyticsFromDatabase();

        verify(redisService, never()).saveTopSpeakers(any());
    }

    @Test
    void testGetAmountOfTalksPerYearValid() {
        when(hashOperations.get(TALKS_PER_YEAR, "2024")).thenReturn("5");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        Long result = tedTalkAnalyticsService.getAmountOfTalksPerYear(2024);

        assertEquals(5L, result);
    }

    @Test
    void testGetAmountOfTalksPerYearInvalid() {
        when(hashOperations.get(TALKS_PER_YEAR, "2024")).thenReturn("invalid");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        Long result = tedTalkAnalyticsService.getAmountOfTalksPerYear(2024);

        assertEquals(0L, result);
    }
}