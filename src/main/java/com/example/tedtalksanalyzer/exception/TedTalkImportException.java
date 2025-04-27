package com.example.tedtalksanalyzer.exception;

public class TedTalkImportException extends RuntimeException {
    public TedTalkImportException(String error, Throwable e) {
        super(error, e);
    }
}
