package com.example.tedtalksanalyzer.service.analytics;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AnalyticsRefreshJobTest {

    @Mock
    private TedTalkAnalyticsService tedTalkAnalyticsService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AnalyticsRefreshJob analyticsRefreshJob;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldRefreshAnalyticsWhenNoLastUpdated() {
        when(valueOperations.get("analytics:last_updated")).thenReturn(null);

        analyticsRefreshJob.scheduledRefreshAnalytics();

        verify(tedTalkAnalyticsService).recalculateAnalyticsFromDatabase();
        verify(valueOperations).set(eq("analytics:last_updated"), anyString());
    }

    @Test
    void shouldNotRefreshIfAnalyticsAreUpToDate() {
        Instant now = Instant.now();
        when(valueOperations.get("analytics:last_updated")).thenReturn(now.toString());

        analyticsRefreshJob.scheduledRefreshAnalytics();

        verify(tedTalkAnalyticsService, never()).recalculateAnalyticsFromDatabase();
    }

    @Test
    void shouldForceRefreshOnInvalidTimestamp() {
        when(valueOperations.get("analytics:last_updated")).thenReturn("invalid-timestamp");

        analyticsRefreshJob.scheduledRefreshAnalytics();

        verify(tedTalkAnalyticsService).recalculateAnalyticsFromDatabase();
        verify(valueOperations).set(eq("analytics:last_updated"), anyString());
    }
}
