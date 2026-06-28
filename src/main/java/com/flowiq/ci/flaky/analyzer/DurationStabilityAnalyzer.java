package com.flowiq.ci.flaky.analyzer;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import lombok.Builder;
import lombok.Value;

import java.util.List;

public class DurationStabilityAnalyzer {

    private final double cvThreshold;
    private final int minSamples;

    public DurationStabilityAnalyzer() {
        this(0.35, 3);
    }

    public DurationStabilityAnalyzer(double cvThreshold, int minSamples) {
        this.cvThreshold = cvThreshold;
        this.minSamples = minSamples;
    }

    public DurationMetrics analyze(List<TestExecutionRecord> runs) {
        List<Long> durations = runs.stream()
                .map(TestExecutionRecord::getDurationMs)
                .filter(d -> d > 0)
                .toList();

        if (durations.size() < minSamples) {
            return DurationMetrics.stable();
        }

        double mean = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        if (mean <= 0) {
            return DurationMetrics.stable();
        }

        double variance = durations.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = stdDev / mean;

        long min = durations.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = durations.stream().mapToLong(Long::longValue).max().orElse(0);

        return DurationMetrics.builder()
                .unstable(cv >= cvThreshold)
                .coefficientOfVariation(cv)
                .avgDurationMs(Math.round(mean))
                .minDurationMs(min)
                .maxDurationMs(max)
                .sampleCount(durations.size())
                .build();
    }

    @Value
    @Builder
    public static class DurationMetrics {
        boolean unstable;
        double coefficientOfVariation;
        long avgDurationMs;
        long minDurationMs;
        long maxDurationMs;
        int sampleCount;

        static DurationMetrics stable() {
            return DurationMetrics.builder()
                    .unstable(false)
                    .coefficientOfVariation(0)
                    .sampleCount(0)
                    .build();
        }
    }
}
