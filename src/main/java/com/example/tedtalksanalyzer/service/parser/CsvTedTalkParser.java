package com.example.tedtalksanalyzer.service.parser;

import com.example.tedtalksanalyzer.exception.CsvParsingException;
import com.example.tedtalksanalyzer.model.TedTalk;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Slf4j
@UtilityClass
public class CsvTedTalkParser {
    private static final int BATCH_SIZE = 500;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    public static void parseCsvByBatch(String absolutePath, Consumer<List<TedTalk>> batchConsumer) {
        Path path = Path.of(absolutePath);
        List<String> errors = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(path.toFile()))) {
            String[] header = csvReader.readNext();
            if (header == null) {
                throw new CsvParsingException("CSV file is empty: " + absolutePath);
            }

            List<TedTalk> buffer = new ArrayList<>();
            String[] row;
            int lineNumber = 1;

            while ((row = csvReader.readNext()) != null) {
                lineNumber++;
                try {
                    TedTalk tedTalk = mapRowToTedTalk(row);
                    buffer.add(tedTalk);

                    if (buffer.size() == BATCH_SIZE) {
                        batchConsumer.accept(new ArrayList<>(buffer));
                        buffer.clear();
                    }

                } catch (Exception e) {
                    errors.add("Invalid line " + lineNumber + ": " + e.getMessage());
                }
            }

            if (!buffer.isEmpty()) {
                batchConsumer.accept(new ArrayList<>(buffer));
            }

            if (!errors.isEmpty()) {
                log.warn("Errors during CSV parsing:");
                errors.forEach(log::error);
            }

        } catch (IOException | CsvValidationException e) {
            throw new CsvParsingException("Failed to read CSV file: " + absolutePath, e);
        }
    }

    private static TedTalk mapRowToTedTalk(String[] row) {
        if (row.length < 6) {
            throw new CsvParsingException("Invalid row: less than 6 fields");
        }

        String title = row[0].trim();
        String author = row[1].trim();
        String dateString = row[2].trim();
        String viewsString = row[3].trim();
        String likesString = row[4].trim();
        String link = row[5].trim();

        LocalDate date = parseDate(dateString);
        Long views = parseLong(viewsString);
        Long likes = parseLong(likesString);

        return new TedTalk(title, author, date, views, likes, link);
    }

    private static LocalDate parseDate(String dateString) {
        try {
            YearMonth yearMonth = YearMonth.parse(dateString, DATE_FORMATTER);
            return yearMonth.atDay(1);
        } catch (Exception e) {
            throw new CsvParsingException("Invalid date format, expected 'MMMM yyyy', got: " + dateString);
        }
    }

    private static Long parseLong(String numberString) {
        try {
            return Long.parseLong(numberString);
        } catch (Exception e) {
            throw new CsvParsingException("Invalid number format: " + numberString);
        }
    }
}
