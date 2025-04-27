package com.example.tedtalksanalyzer.exception;

public class CsvParsingException extends RuntimeException {
    public CsvParsingException(String message, Exception exception) {
        super(message, exception);
    }

    public CsvParsingException(String message) {
        super(message);
    }
}
