package com.flowiq.listeners;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InfrastructureRetryAnalyzerTest {

    @Test(groups = "unit")
    public void shouldRetryConnectionFailures() {
        Assert.assertTrue(InfrastructureRetryAnalyzer.isInfrastructureFailure(
                new RuntimeException("Connection refused: localhost:8080")));
    }

    @Test(groups = "unit")
    public void shouldNotRetryAssertionFailures() {
        Assert.assertFalse(InfrastructureRetryAnalyzer.isInfrastructureFailure(
                new AssertionError("Expected 200 but was 404")));
    }
}
