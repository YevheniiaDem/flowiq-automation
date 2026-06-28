package com.flowiq.listeners;

import com.flowiq.support.RetrySupport;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries only infrastructure-level failures (network, timeouts, 5xx).
 * Business assertion failures are never retried.
 */
public class InfrastructureRetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_ATTEMPTS = 2;
    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (attempt >= MAX_ATTEMPTS) {
            return false;
        }
        Throwable throwable = result.getThrowable();
        if (throwable == null || !isInfrastructureFailure(throwable)) {
            return false;
        }
        attempt++;
        return true;
    }

    static boolean isInfrastructureFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RetrySupport.RetryableApiException) {
                return true;
            }
            String message = String.valueOf(current.getMessage()).toLowerCase();
            if (message.contains("connection")
                    || message.contains("timeout")
                    || message.contains("timed out")
                    || message.contains("refused")
                    || message.contains("net::err")
                    || message.contains("503")
                    || message.contains("502")
                    || message.contains("504")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
