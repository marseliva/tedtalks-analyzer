package com.example.tedtalksanalyzer.controller;

import com.example.tedtalksanalyzer.event.TedTalkImportEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@Slf4j
public class TedTalkImportController {

    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ApplicationEventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Uploaded file is empty");
        }

        try {
            Path tempFile = saveTempFile(file);

            eventPublisher.publishEvent(new TedTalkImportEvent(tempFile.toAbsolutePath().toString()));
            log.info("File uploaded and event published: {}", tempFile);

            return ResponseEntity.accepted().body("File received. Processing started.");
        } catch (IOException e) {
            log.error("Failed to save uploaded file", e);
            return ResponseEntity.internalServerError().body("Failed to process file");
        }
    }

    private Path saveTempFile(MultipartFile file) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT);
        Path tempFile = Files.createTempFile("tedtalks_" + timestamp + "_", ".csv");
        file.transferTo(tempFile.toFile());
        return tempFile;
    }
}