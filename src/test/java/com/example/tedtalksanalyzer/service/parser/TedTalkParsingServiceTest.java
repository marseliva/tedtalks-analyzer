package com.example.tedtalksanalyzer.service.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.tedtalksanalyzer.event.TedTalkImportEvent;
import com.example.tedtalksanalyzer.exception.TedTalkImportException;
import com.example.tedtalksanalyzer.service.TedTalkService;
import com.example.tedtalksanalyzer.service.analytics.TedTalkAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class TedTalkParsingServiceTest {

    @Mock
    private TedTalkService tedTalkService;

    @Mock
    private TedTalkAnalyticsService analyticsService;

    @InjectMocks
    private TedTalkParsingService parsingService;

    private Path resourceFile;

    @BeforeEach
    void setUp() throws IOException {
        resourceFile = Files.createTempFile("tedtalks", ".csv");

        try (var inputStream = getClass().getClassLoader().getResourceAsStream("test.csv")) {
            if (inputStream == null) {
                throw new IOException("Resource file not found");
            }
            Files.write(resourceFile, inputStream.readAllBytes());
        }
    }

    @Test
    void testSuccessfulImport() {
        TedTalkImportEvent event = new TedTalkImportEvent(resourceFile.toAbsolutePath().toString());

        parsingService.importFromCsv(event);

        verify(tedTalkService, atLeastOnce()).saveAll(any());
        verify(analyticsService).calculateAndStoreAnalytics(anyList());
    }

    @Test
    void testImportThrowsWhenSaveFails() {
        TedTalkImportEvent event = new TedTalkImportEvent(resourceFile.toAbsolutePath().toString());

        doThrow(new RuntimeException("Save failed")).when(tedTalkService).saveAll(anyList());

        assertThrows(TedTalkImportException.class, () -> parsingService.importFromCsv(event));

        verify(tedTalkService, atLeastOnce()).saveAll(any());
    }
}