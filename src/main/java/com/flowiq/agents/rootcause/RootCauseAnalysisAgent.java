package com.flowiq.agents.rootcause;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.rootcause.analyzer.RootCauseAnalyzerPipeline;
import com.flowiq.agents.rootcause.config.RootCauseAgentConfig;
import com.flowiq.agents.rootcause.loader.FailureArtifactAggregator;
import com.flowiq.agents.rootcause.model.FailedTestContext;
import com.flowiq.agents.rootcause.model.RootCauseAnalysisResult;
import com.flowiq.agents.rootcause.model.RootCauseFinding;
import com.flowiq.agents.rootcause.report.RootCauseAnalysisReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AI agent that determines the most probable root cause of test failures by correlating
 * Allure results, Surefire reports, screenshots, traces, videos, and backend logs.
 */
@Slf4j
public class RootCauseAnalysisAgent {

    private final RootCauseAgentConfig config;
    private final FailureArtifactAggregator artifactAggregator;
    private final RootCauseAnalyzerPipeline analyzerPipeline;
    private final RootCauseAnalysisReportGenerator reportGenerator;

    public RootCauseAnalysisAgent() {
        this(ConfigFactory.create(RootCauseAgentConfig.class));
    }

    public RootCauseAnalysisAgent(RootCauseAgentConfig config) {
        this.config = config;
        ObjectMapper objectMapper = new ObjectMapper();
        this.artifactAggregator = new FailureArtifactAggregator(config, objectMapper);
        this.analyzerPipeline = new RootCauseAnalyzerPipeline();
        this.reportGenerator = new RootCauseAnalysisReportGenerator(config);
    }

    public RootCauseAnalysisResult run() {
        log.info("Starting RootCauseAnalysisAgent");
        return run(artifactAggregator.loadFailedTests(), artifactAggregator.summarizeSources());
    }

    public RootCauseAnalysisResult run(List<FailedTestContext> failures, String dataSourcesSummary) {
        List<RootCauseFinding> findings = new ArrayList<>();
        for (FailedTestContext failure : failures) {
            findings.add(analyzerPipeline.analyze(failure));
        }
        findings.sort(Comparator.comparingInt(RootCauseFinding::getConfidence).reversed());
        if (findings.size() > config.topN()) {
            findings = findings.subList(0, config.topN());
        }

        int highConfidence = (int) findings.stream().filter(f -> f.getConfidence() >= 70).count();
        List<String> summary = buildExecutiveSummary(findings, highConfidence);

        var resultBuilder = RootCauseAnalysisResult.builder()
                .analyzedAt(Instant.now())
                .failuresAnalyzed(failures.size())
                .highConfidenceFindings(highConfidence)
                .dataSourcesSummary(dataSourcesSummary);
        findings.forEach(resultBuilder::finding);
        summary.forEach(resultBuilder::summaryLine);
        RootCauseAnalysisResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Root cause analysis complete. {} failure(s), report={}",
                failures.size(), reportPath.toAbsolutePath());
        return result;
    }

    private static List<String> buildExecutiveSummary(List<RootCauseFinding> findings, int highConfidence) {
        List<String> summary = new ArrayList<>();
        summary.add(findings.size() + " failed test(s) analyzed with artifact and log correlation.");
        summary.add(highConfidence + " finding(s) reached ≥70% confidence.");
        if (!findings.isEmpty()) {
            RootCauseFinding top = findings.get(0);
            summary.add(String.format("Top hypothesis: %s — %s (%d%% confidence).",
                    top.getFailedTest(),
                    top.getMostProbableRootCause(),
                    top.getConfidence()));
        }
        long uiFailures = findings.stream()
                .filter(f -> f.getMostProbableRootCause().name().equals("UI_BUG"))
                .count();
        if (uiFailures > 0) {
            summary.add(uiFailures + " failure(s) classified as UI_BUG — review traces and screenshots first.");
        }
        return summary;
    }

    public static void main(String[] args) {
        new RootCauseAnalysisAgent().run();
    }
}
