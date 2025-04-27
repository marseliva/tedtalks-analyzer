package com.example.tedtalksanalyzer.exception;

public class AnalyticsCacheException extends RuntimeException {
    public AnalyticsCacheException(String message, Exception exception) {
        super(message, exception);
    }

    public AnalyticsCacheException(String message) {
        super(message);
    }
}
