package com.flowiq.agents.flaky;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.aggregator.StabilityMetricsCalculator;
import com.flowiq.agents.flaky.analyzer.RootCauseAnalyzerPipeline;
import com.flowiq.agents.flaky.config.FlakyTestAgentConfig;
import com.flowiq.agents.flaky.loader.CiHistoryLoader;
import com.flowiq.agents.flaky.loader.TestExecutionAggregator;
import com.flowiq.agents.flaky.model.FlakyInvestigationResult;
import com.flowiq.agents.flaky.model.FlakyTestFinding;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestStabilityMetrics;
import com.flowiq.agents.flaky.prioritizer.FlakyTestPriorityScorer;
import com.flowiq.agents.flaky.report.FlakyTestReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI agent that investigates test instability by correlating Allure history,
 * Surefire reports, GitHub Actions runs, and execution logs.
 */
@Slf4j
public class FlakyTestInvestigator {

    private final FlakyTestAgentConfig config;
    private final ObjectMapper objectMapper;
    private final TestExecutionAggregator aggregator;
    private final StabilityMetricsCalculator metricsCalculator;
    private final CiHistoryLoader ciHistoryLoader;
    private final RootCauseAnalyzerPipeline rootCausePipeline;
    private final FlakyTestPriorityScorer priorityScorer;
    private final FlakyTestReportGenerator reportGenerator;

    public FlakyTestInvestigator() {
        this(ConfigFactory.create(FlakyTestAgentConfig.class));
    }

    public FlakyTestInvestigator(FlakyTestAgentConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.aggregator = new TestExecutionAggregator(config, objectMapper);
        this.metricsCalculator = new StabilityMetricsCalculator();
        this.ciHistoryLoader = new CiHistoryLoader(config, objectMapper);
        this.rootCausePipeline = new RootCauseAnalyzerPipeline();
        this.priorityScorer = new FlakyTestPriorityScorer();
        this.reportGenerator = new FlakyTestReportGenerator(config);
    }

    public FlakyInvestigationResult run() {
        log.info("Starting FlakyTestInvestigator");
        return run(aggregator.loadAll(), aggregator.summarizeSources());
    }

    public FlakyInvestigationResult run(List<TestExecutionRecord> records, String dataSourcesSummary) {
        Map<String, Integer> ciFailures = ciHistoryLoader.failureCountsByTest();

        List<TestStabilityMetrics> metrics = metricsCalculator.calculate(records, config.minRuns());
        StabilityMetricsCalculator.PortfolioMetrics portfolio = metricsCalculator.portfolio(metrics);

        List<FlakyTestFinding> findings = buildFindings(records, metrics, ciFailures);
        findings = findings.stream()
                .sorted(Comparator.comparing(FlakyTestFinding::getPriorityScore).reversed())
                .limit(config.topN())
                .collect(Collectors.toList());

        List<String> executiveSummary = buildExecutiveSummary(portfolio, findings);

        var resultBuilder = FlakyInvestigationResult.builder()
                .analyzedAt(Instant.now())
                .totalExecutionsAnalyzed(records.size())
                .uniqueTests(metrics.size())
                .flakyTestCount((int) metrics.stream().filter(TestStabilityMetrics::isFlaky).count())
                .portfolioPassRate(portfolio.passRate())
                .portfolioFailureRate(portfolio.failureRate())
                .portfolioFlakinessPercent(portfolio.flakinessPercent())
                .topUnstableTests(findings)
                .dataSourcesSummary(dataSourcesSummary);
        executiveSummary.forEach(resultBuilder::summaryLine);
        FlakyInvestigationResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Investigation complete. {} flaky test(s), report={}",
                result.getFlakyTestCount(), reportPath.toAbsolutePath());
        return result;
    }

    private List<FlakyTestFinding> buildFindings(List<TestExecutionRecord> records,
                                                  List<TestStabilityMetrics> metrics,
                                                  Map<String, Integer> ciFailures) {
        Map<String, List<TestExecutionRecord>> byKey = records.stream()
                .collect(Collectors.groupingBy(TestExecutionRecord::getTestKey));

        List<FlakyTestFinding> findings = new ArrayList<>();
        for (TestStabilityMetrics metric : metrics) {
            if (!metric.isFlaky() && metric.getFailureRate() < 5.0) {
                continue;
            }
            List<TestExecutionRecord> runs = byKey.getOrDefault(metric.getTestKey(), List.of());
            List<TestExecutionRecord> failures = runs.stream()
                    .filter(r -> r.getOutcome().isFailure())
                    .collect(Collectors.toList());

            var rootCause = rootCausePipeline.analyze(failures);
            String lastFailure = failures.isEmpty() ? ""
                    : failures.get(failures.size() - 1).getMessage();

            int ciCount = ciFailures.getOrDefault(metric.getTestKey(), 0)
                    + ciFailures.getOrDefault(metric.getMethodName(), 0);

            FlakyTestFinding finding = FlakyTestFinding.builder()
                    .metrics(metric)
                    .primaryRootCause(rootCause.primary())
                    .alternateCauses(rootCause.alternates())
                    .recommendedFix(rootCausePipeline.recommendFix(rootCause.primary(), metric.getSuite()))
                    .ciFailureCount(ciCount)
                    .lastFailureMessage(lastFailure)
                    .build();

            findings.add(priorityScorer.withScore(finding));
        }
        return findings;
    }

    private List<String> buildExecutiveSummary(StabilityMetricsCalculator.PortfolioMetrics portfolio,
                                               List<FlakyTestFinding> topFindings) {
        List<String> summary = new ArrayList<>();
        summary.add(String.format("Portfolio pass rate is %.1f%% with %.1f%% failure rate across analyzed runs.",
                portfolio.passRate(), portfolio.failureRate()));
        summary.add(String.format("%d flaky tests identified (%.1f%% portfolio flakiness).",
                portfolio.flakyTestCount(), portfolio.flakinessPercent()));
        if (!topFindings.isEmpty()) {
            FlakyTestFinding top = topFindings.get(0);
            summary.add(String.format("Highest priority: `%s` (score %.1f, %s).",
                    top.getMetrics().getMethodName(),
                    top.getPriorityScore(),
                    top.getPrimaryRootCause().getType()));
        }
        long uiFlaky = topFindings.stream().filter(f -> "ui".equals(f.getMetrics().getSuite())).count();
        if (uiFlaky > 0) {
            summary.add(uiFlaky + " of top unstable tests are UI smoke — prioritize locator and timeout hardening.");
        }
        return summary;
    }

    public static void main(String[] args) {
        new FlakyTestInvestigator().run();
    }
}
