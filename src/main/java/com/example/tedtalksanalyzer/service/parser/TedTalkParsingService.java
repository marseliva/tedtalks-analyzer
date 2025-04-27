package com.example.tedtalksanalyzer.service.parser;

import com.example.tedtalksanalyzer.dto.TedTalkAnalyticsDTO;
import com.example.tedtalksanalyzer.event.TedTalkImportEvent;
import com.example.tedtalksanalyzer.exception.TedTalkImportException;
import com.example.tedtalksanalyzer.model.TedTalk;
import com.example.tedtalksanalyzer.service.TedTalkDataService;
import com.example.tedtalksanalyzer.service.analytics.TedTalkAnalyticsService;
import com.example.tedtalksanalyzer.service.utils.ExecutorServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TedTalkParsingService {

    private static final String GENERAL_CSV_IMPORT_ERROR = "General error during CSV import";

    private final TedTalkDataService tedTalkDataService;
    private final TedTalkAnalyticsService analyticsService;

    @EventListener
    public void importFromCsv(TedTalkImportEvent event) {
        Path tempFile = Path.of(event.absolutePath());
        List<TedTalk> importedTalks = new CopyOnWriteArrayList<>();
        ExecutorService executorService = ExecutorServiceUtils.createExecutorService();
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        try {
            parseAndSaveCsvAsync(tempFile, importedTalks, executorService, errors);
            validateImport(errors);
            TedTalkAnalyticsDTO tedTalkAnalyticsDTO = analyticsService.calculateAndStoreAnalytics(importedTalks);
            if (tedTalkAnalyticsDTO != null) {
                log.info("Analytics for file " + tedTalkAnalyticsDTO);
            }
        } catch (Exception e) {
            log.error(GENERAL_CSV_IMPORT_ERROR, e);
            throw new TedTalkImportException(GENERAL_CSV_IMPORT_ERROR, e);
        } finally {
            ExecutorServiceUtils.forceShutdownExecutor(executorService);
            safelyDeleteTempFile(tempFile);
        }
    }

    private void parseAndSaveCsvAsync(Path file, List<TedTalk> allTedTalks, ExecutorService executor, List<Throwable> errors) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        CsvTedTalkParser.parseCsvByBatch(file.toString(), batch -> {
            List<TedTalk> batchCopy = new ArrayList<>(batch);
            allTedTalks.addAll(batchCopy);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                    tedTalkDataService.saveAll(batchCopy), executor
            ).exceptionally(ex -> {
                log.error("Error saving batch", ex);
                errors.add(ex);
                return null;
            });

            futures.add(future);
        });

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void validateImport(List<Throwable> errors) {
        if (!errors.isEmpty()) {
            throw new TedTalkImportException("Errors occurred during batch saving", errors.get(0));
        }
    }

    private void safelyDeleteTempFile(Path tempFile) {
        try {
            if (Files.deleteIfExists(tempFile)) {
                log.info("Temp file deleted: {}", tempFile);
            } else {
                log.warn("Temp file not found for deletion: {}", tempFile);
            }
        } catch (IOException e) {
            log.error("Failed to delete temp file: {}", tempFile, e);
        }
    }
}