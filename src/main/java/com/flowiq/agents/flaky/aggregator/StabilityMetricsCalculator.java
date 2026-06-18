package com.flowiq.agents.flaky.aggregator;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestStabilityMetrics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StabilityMetricsCalculator {

    public List<TestStabilityMetrics> calculate(List<TestExecutionRecord> records, int minRuns) {
        Map<String, List<TestExecutionRecord>> grouped = new LinkedHashMap<>();
        for (TestExecutionRecord record : records) {
            grouped.computeIfAbsent(record.getTestKey(), key -> new ArrayList<>()).add(record);
        }

        List<TestStabilityMetrics> metrics = new ArrayList<>();
        for (Map.Entry<String, List<TestExecutionRecord>> entry : grouped.entrySet()) {
            List<TestExecutionRecord> runs = entry.getValue();
            if (runs.size() < minRuns) {
                continue;
            }
            TestExecutionRecord sample = runs.get(0);
            int pass = 0;
            int fail = 0;
            int broken = 0;
            int skip = 0;
            for (TestExecutionRecord run : runs) {
                switch (run.getOutcome()) {
                    case PASSED -> pass++;
                    case FAILED -> fail++;
                    case BROKEN -> broken++;
                    case SKIPPED -> skip++;
                }
            }
            int decisive = pass + fail + broken;
            double passRate = decisive == 0 ? 0.0 : (pass * 100.0) / decisive;
            double failureRate = decisive == 0 ? 0.0 : ((fail + broken) * 100.0) / decisive;
            double flakiness = calculateFlakiness(pass, fail + broken);
            boolean flaky = pass > 0 && (fail + broken) > 0;

            metrics.add(TestStabilityMetrics.builder()
                    .testKey(entry.getKey())
                    .className(sample.getClassName())
                    .methodName(sample.getMethodName())
                    .suite(sample.getSuite())
                    .totalRuns(runs.size())
                    .passCount(pass)
                    .failCount(fail)
                    .brokenCount(broken)
                    .skipCount(skip)
                    .passRate(passRate)
                    .failureRate(failureRate)
                    .flakinessPercent(flakiness)
                    .flaky(flaky)
                    .build());
        }
        return metrics;
    }

    public static double calculateFlakiness(int passCount, int failCount) {
        if (passCount == 0 || failCount == 0) {
            return 0.0;
        }
        int total = passCount + failCount;
        return (Math.min(passCount, failCount) * 100.0) / total;
    }

    public PortfolioMetrics portfolio(List<TestStabilityMetrics> metrics) {
        if (metrics.isEmpty()) {
            return new PortfolioMetrics(0, 0, 0, 0, 0);
        }
        int totalRuns = metrics.stream().mapToInt(TestStabilityMetrics::getTotalRuns).sum();
        int totalPass = metrics.stream().mapToInt(TestStabilityMetrics::getPassCount).sum();
        int totalFail = metrics.stream().mapToInt(m -> m.getFailCount() + m.getBrokenCount()).sum();
        int flakyCount = (int) metrics.stream().filter(TestStabilityMetrics::isFlaky).count();
        int decisive = totalPass + totalFail;
        double passRate = decisive == 0 ? 0 : totalPass * 100.0 / decisive;
        double failureRate = decisive == 0 ? 0 : totalFail * 100.0 / decisive;
        double flakiness = calculateFlakiness(totalPass, totalFail);
        return new PortfolioMetrics(totalRuns, passRate, failureRate, flakyCount, flakiness);
    }

    public record PortfolioMetrics(int totalRuns, double passRate, double failureRate,
                                   int flakyTestCount, double flakinessPercent) {
    }
}
