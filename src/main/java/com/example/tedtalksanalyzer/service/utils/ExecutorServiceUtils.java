package com.example.tedtalksanalyzer.service.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@UtilityClass
public class ExecutorServiceUtils {

    private static final int AWAIT_TERMINATION_SECONDS = 60;

    public ExecutorService createExecutorService() {
        return Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
    }

    public void forceShutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(AWAIT_TERMINATION_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                log.warn("Executor service forced to shutdown");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Executor service shutdown interrupted", e);
        }
    }
}
