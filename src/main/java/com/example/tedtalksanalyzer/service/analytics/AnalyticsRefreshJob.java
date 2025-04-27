package com.example.tedtalksanalyzer.service.analytics;

import com.example.tedtalksanalyzer.exception.AnalyticsCacheException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsRefreshJob {

    private static final String LAST_UPDATED_KEY = "analytics:last_updated";
    private static final Duration REFRESH_THRESHOLD = Duration.ofHours(24);

    private final TedTalkAnalyticsService tedTalkAnalyticsService;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledRefreshAnalytics() {
        try {
            if (needsRefresh()) {
                refreshAnalytics();
            } else {
                log.info("Analytics up-to-date, no refresh needed.");
            }
        } catch (Exception e) {
            log.error("Unexpected error during analytics refresh check", e);
        }
    }

    private boolean needsRefresh() {
        String lastUpdatedStr = redisTemplate.opsForValue().get(LAST_UPDATED_KEY);

        if (!StringUtils.hasText(lastUpdatedStr)) {
            log.info("No previous analytics found, needs refresh.");
            return true;
        }

        try {
            if (!lastUpdatedStr.endsWith("Z")) {
                lastUpdatedStr += "Z";
            }
            Instant lastUpdated = Instant.parse(lastUpdatedStr);
            Instant now = Instant.now();
            boolean needsRefresh = Duration.between(lastUpdated, now).compareTo(REFRESH_THRESHOLD) >= 0;

            log.info("Last updated: {}, now: {}, needs refresh: {}", lastUpdated, now, needsRefresh);
            return needsRefresh;

        } catch (Exception e) {
            log.warn("Invalid last updated timestamp found, forcing refresh.", e);
            return true;
        }
    }


    private void refreshAnalytics() {
        try {
            log.info("Starting analytics refresh...");
            tedTalkAnalyticsService.recalculateAnalyticsFromDatabase();
            redisTemplate.opsForValue().set(LAST_UPDATED_KEY, Instant.now().toString());
            log.info("Analytics successfully refreshed at {}", Instant.now());
        } catch (Exception e) {
            log.error("Failed to refresh analytics", e);
            throw new AnalyticsCacheException("Analytics refresh failed", e);
        }
    }
}
