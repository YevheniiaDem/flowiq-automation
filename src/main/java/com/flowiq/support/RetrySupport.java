package com.flowiq.support;

import com.flowiq.clients.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
public final class RetrySupport {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_DELAY_MS = 1_000L;
    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(408, 429, 500, 502, 503, 504);

    private RetrySupport() {
    }

    public static <T> T execute(Supplier<T> action) {
        return execute(action, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }

    public static <T> T execute(Supplier<T> action, int maxAttempts, long delayMs) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException ex) {
                lastException = ex;
                if (attempt < maxAttempts && isRetryable(ex)) {
                    log.warn("Attempt {}/{} failed: {}. Retrying in {} ms...",
                            attempt, maxAttempts, ex.getMessage(), delayMs);
                    sleep(delayMs);
                } else {
                    break;
                }
            }
        }

        throw lastException != null ? lastException : new IllegalStateException("Retry failed without exception");
    }

    public static ApiResponse executeApi(Supplier<ApiResponse> action) {
        return executeApi(action, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
    }

    public static ApiResponse executeApi(Supplier<ApiResponse> action, int maxAttempts, long delayMs) {
        return execute(() -> {
            ApiResponse response = action.get();
            if (shouldRetry(response)) {
                throw new RetryableApiException("Retryable status: " + response.getStatusCode());
            }
            return response;
        }, maxAttempts, delayMs);
    }

    public static ApiResponse executeApiUntil(Supplier<ApiResponse> action,
                                              Predicate<ApiResponse> successCondition,
                                              int maxAttempts,
                                              long delayMs) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ApiResponse response = action.get();
                if (successCondition.test(response)) {
                    return response;
                }
                throw new RetryableApiException("Condition not met, status: " + response.getStatusCode());
            } catch (RuntimeException ex) {
                lastException = ex;
                if (attempt < maxAttempts) {
                    log.warn("Attempt {}/{}: {}. Retrying...", attempt, maxAttempts, ex.getMessage());
                    sleep(delayMs);
                }
            }
        }

        throw lastException != null ? lastException : new IllegalStateException("Condition not met after retries");
    }

    private static boolean shouldRetry(ApiResponse response) {
        return RETRYABLE_STATUS_CODES.contains(response.getStatusCode());
    }

    private static boolean isRetryable(RuntimeException ex) {
        if (ex instanceof RetryableApiException) {
            return true;
        }
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        return message.contains("connection") || message.contains("timeout") || message.contains("refused");
    }

    private static void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", e);
        }
    }

    public static class RetryableApiException extends RuntimeException {
        public RetryableApiException(String message) {
            super(message);
        }
    }
}
