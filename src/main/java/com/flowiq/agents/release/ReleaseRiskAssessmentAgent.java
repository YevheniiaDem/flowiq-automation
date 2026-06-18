package com.flowiq.agents.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.release.analyzer.BlockedAreaAnalyzer;
import com.flowiq.agents.release.analyzer.CriticalFailureAnalyzer;
import com.flowiq.agents.release.analyzer.RecommendedActionAnalyzer;
import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.loader.ReleaseTestResultsLoader;
import com.flowiq.agents.release.model.ApiChangeReportInsight;
import com.flowiq.agents.release.model.BlockedArea;
import com.flowiq.agents.release.model.CriticalFailure;
import com.flowiq.agents.release.model.FlakyReportInsight;
import com.flowiq.agents.release.model.ReleaseRiskAssessmentResult;
import com.flowiq.agents.release.model.SuiteExecutionSummary;
import com.flowiq.agents.release.parser.ApiChangeReportParser;
import com.flowiq.agents.release.parser.FlakyReportParser;
import com.flowiq.agents.release.report.ReleaseReadinessReportGenerator;
import com.flowiq.agents.release.scorer.ReleaseRiskScoreCalculator;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI agent that assesses release readiness by correlating regression, smoke,
 * and contract results with flaky-test and API-change reports.
 */
@Slf4j
public class ReleaseRiskAssessmentAgent {

    private final ReleaseRiskAgentConfig config;
    private final ReleaseTestResultsLoader resultsLoader;
    private final FlakyReportParser flakyReportParser;
    private final ApiChangeReportParser apiChangeReportParser;
    private final CriticalFailureAnalyzer criticalFailureAnalyzer;
    private final BlockedAreaAnalyzer blockedAreaAnalyzer;
    private final ReleaseRiskScoreCalculator scoreCalculator;
    private final RecommendedActionAnalyzer actionAnalyzer;
    private final ReleaseReadinessReportGenerator reportGenerator;

    public ReleaseRiskAssessmentAgent() {
        this(ConfigFactory.create(ReleaseRiskAgentConfig.class));
    }

    public ReleaseRiskAssessmentAgent(ReleaseRiskAgentConfig config) {
        this.config = config;
        ObjectMapper objectMapper = new ObjectMapper();
        this.resultsLoader = new ReleaseTestResultsLoader(config, objectMapper);
        this.flakyReportParser = new FlakyReportParser(config);
        this.apiChangeReportParser = new ApiChangeReportParser(config);
        this.criticalFailureAnalyzer = new CriticalFailureAnalyzer();
        this.blockedAreaAnalyzer = new BlockedAreaAnalyzer();
        this.scoreCalculator = new ReleaseRiskScoreCalculator(config);
        this.actionAnalyzer = new RecommendedActionAnalyzer();
        this.reportGenerator = new ReleaseReadinessReportGenerator(config);
    }

    public ReleaseRiskAssessmentResult run() {
        log.info("Starting ReleaseRiskAssessmentAgent");
        List<TestExecutionRecord> records = resultsLoader.loadAllRecords();
        return run(records, resultsLoader.summarizeSources(),
                flakyReportParser.parse(), apiChangeReportParser.parse());
    }

    public ReleaseRiskAssessmentResult run(List<TestExecutionRecord> records,
                                           String dataSourcesSummary,
                                           FlakyReportInsight flakyInsight,
                                           ApiChangeReportInsight apiChangeInsight) {
        Map<TestSuiteType, SuiteExecutionSummary> summaries =
                resultsLoader.summarize(records, dataSourcesSummary);

        SuiteExecutionSummary regression = summaries.get(TestSuiteType.REGRESSION);
        SuiteExecutionSummary smoke = summaries.get(TestSuiteType.SMOKE);
        SuiteExecutionSummary contract = summaries.get(TestSuiteType.CONTRACT);

        List<CriticalFailure> criticalFailures = criticalFailureAnalyzer.analyze(records);
        List<BlockedArea> blockedAreas = blockedAreaAnalyzer.analyze(criticalFailures);

        ReleaseRiskScoreCalculator.ScoreResult score = scoreCalculator.calculate(
                regression, smoke, contract, flakyInsight, apiChangeInsight, criticalFailures);

        List<String> actions = actionAnalyzer.generate(
                regression, smoke, contract, flakyInsight, apiChangeInsight,
                criticalFailures, blockedAreas, score.category(), score.recommendation());

        List<String> executiveSummary = buildExecutiveSummary(
                score, regression, smoke, contract, flakyInsight, apiChangeInsight, criticalFailures);

        var resultBuilder = ReleaseRiskAssessmentResult.builder()
                .assessedAt(Instant.now())
                .releaseRiskScore(score.score())
                .riskCategory(score.category())
                .recommendation(score.recommendation())
                .regressionSummary(regression)
                .smokeSummary(smoke)
                .contractSummary(contract)
                .flakyInsight(flakyInsight)
                .apiChangeInsight(apiChangeInsight)
                .criticalFailures(criticalFailures)
                .blockedAreas(blockedAreas)
                .recommendedActions(actions)
                .scoreBreakdown(score.breakdown())
                .dataSourcesSummary(dataSourcesSummary);
        executiveSummary.forEach(resultBuilder::summaryLine);
        ReleaseRiskAssessmentResult result = resultBuilder.build();

        Path reportPath = reportGenerator.generate(result);
        log.info("Assessment complete. Score={}, category={}, recommendation={}, report={}",
                score.score(), score.category(), score.recommendation(), reportPath.toAbsolutePath());
        return result;
    }

    private List<String> buildExecutiveSummary(ReleaseRiskScoreCalculator.ScoreResult score,
                                               SuiteExecutionSummary regression,
                                               SuiteExecutionSummary smoke,
                                               SuiteExecutionSummary contract,
                                               FlakyReportInsight flaky,
                                               ApiChangeReportInsight apiChange,
                                               List<CriticalFailure> criticalFailures) {
        List<String> summary = new ArrayList<>();
        summary.add(String.format("Release risk score is %.1f (%s) — recommendation: %s.",
                score.score(), score.category(), score.recommendation()));
        summary.add(String.format("Regression %.1f%% | Smoke %.1f%% | Contract %.1f%% pass rate.",
                regression.getPassRate(), smoke.getPassRate(), contract.getPassRate()));
        if (!criticalFailures.isEmpty()) {
            summary.add(criticalFailures.size() + " critical failure(s) across gate suites.");
        }
        if (flaky.isReportFound() && flaky.getFlakyTestCount() > 0) {
            summary.add(flaky.getFlakyTestCount() + " flaky test(s) may affect release stability.");
        }
        if (apiChange.isReportFound() && apiChange.getBreakingChanges() > 0) {
            summary.add(apiChange.getBreakingChanges() + " breaking API change(s) require validation.");
        }
        return summary;
    }

    public static void main(String[] args) {
        new ReleaseRiskAssessmentAgent().run();
    }
}
