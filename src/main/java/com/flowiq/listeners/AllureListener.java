package com.flowiq.listeners;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class AllureListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        log.info("Test suite started: {}", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("Test started: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("Test passed: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.error("Test failed: {}", testName, result.getThrowable());
        attachFailureDetails(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("Test skipped: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Test suite finished: {}", context.getName());
    }

    private void attachFailureDetails(ITestResult result) {
        if (result.getThrowable() != null) {
            Allure.addAttachment(
                    "Failure details",
                    "text/plain",
                    result.getThrowable().getMessage() != null
                            ? result.getThrowable().getMessage()
                            : result.getThrowable().toString()
            );
        }
    }
}
