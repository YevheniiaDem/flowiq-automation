package com.flowiq.ci.flaky;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flowiq.agents.flaky.aggregator.StabilityMetricsCalculator;
import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import com.flowiq.agents.flaky.model.TestStabilityMetrics;
import com.flowiq.ci.flaky.analyzer.DurationStabilityAnalyzer;
import com.flowiq.ci.flaky.filter.BusinessTestExecutionFilter;
import com.flowiq.ci.flaky.history.FlakyHistoryStore;
import com.flowiq.ci.flaky.model.CiFlakyReport;
import com.flowiq.ci.flaky.model.CiFlakyTestEntry;
import com.flowiq.ci.flaky.model.FlakyClassification;
import com.flowiq.ci.flaky.report.CiFlakyHtmlReportWriter;
import com.flowiq.ci.flaky.report.CiFlakyJsonReportWriter;
import com.flowiq.ci.flaky.report.CiFlakySummaryWriter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Classifies business-test instability across nightly CI runs.
 * Does not rerun tests and does not treat CI infrastructure retries as flaky signals.
 */
@Slf4j
public class CiFlakyTestAnalyzer {

    private final ObjectMapper objectMapper;
    private final StabilityMetricsCalculator metricsCalculator;
    private final DurationStabilityAnalyzer durationAnalyzer;
    private final FlakyHistoryStore historyStore;
    private final CiFlakyJsonReportWriter jsonWriter;
    private final CiFlakyHtmlReportWriter htmlWriter;
    private final CiFlakySummaryWriter summaryWriter;

    private final int minRuns;
    private final int maxHistoryRuns;

    public CiFlakyTestAnalyzer() {
        this(2, 30);
    }

    public CiFlakyTestAnalyzer(int minRuns, int maxHistoryRuns) {
        this.minRuns = minRuns;
        this.maxHistoryRuns = maxHistoryRuns;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.metricsCalculator = new StabilityMetricsCalculator();
        this.durationAnalyzer = new DurationStabilityAnalyzer();
        this.historyStore = new FlakyHistoryStore(maxHistoryRuns);
        this.jsonWriter = new CiFlakyJsonReportWriter(objectMapper);
        this.htmlWriter = new CiFlakyHtmlReportWriter();
        this.summaryWriter = new CiFlakySummaryWriter();
    }

    public CiFlakyReport analyze(CiFlakyAnalysisRequest request) throws Exception {
        List<TestExecutionRecord> currentRun = loadCurrentRun(request.allureResultsDir());
        currentRun = BusinessTestExecutionFilter.filterBusinessTests(currentRun);

        FlakyHistoryStore.FlakyHistoryDocument historyDoc =
                historyStore.load(request.historyFile());

        List<TestExecutionRecord> historical = historyStore.toRecords(historyDoc);
        historical = BusinessTestExecutionFilter.filterBusinessTests(historical);

        List<TestExecutionRecord> combined = new ArrayList<>(historical);
        combined.addAll(tagCurrentRun(currentRun, request.runId()));

        Map<String, List<TestExecutionRecord>> byKey = combined.stream()
                .collect(Collectors.groupingBy(TestExecutionRecord::getTestKey, LinkedHashMap::new, Collectors.toList()));

        Map<String, TestExecutionRecord> currentByKey = currentRun.stream()
                .collect(Collectors.toMap(TestExecutionRecord::getTestKey, r -> r, (a, b) -> b, LinkedHashMap::new));

        List<TestStabilityMetrics> metrics = metricsCalculator.calculate(combined, minRuns);

        Set<String> flakyKeys = new LinkedHashSet<>();
        List<CiFlakyTestEntry> flakyEntries = new ArrayList<>();
        for (TestStabilityMetrics metric : metrics) {
            List<TestExecutionRecord> runs = byKey.getOrDefault(metric.getTestKey(), List.of());
            DurationStabilityAnalyzer.DurationMetrics duration = durationAnalyzer.analyze(runs);

            boolean intermittent = metric.isFlaky();
            boolean durationUnstable = duration.isUnstable();
            if (!intermittent && !durationUnstable) {
                continue;
            }

            TestExecutionRecord current = currentByKey.get(metric.getTestKey());
            boolean failedNow = current != null && current.getOutcome().isFailure();
            boolean passedNow = current != null && current.getOutcome() == TestOutcome.PASSED;
            boolean recovered = passedNow && metric.getFailCount() + metric.getBrokenCount() > 0;

            FlakyClassification classification = classify(intermittent, durationUnstable);
            CiFlakyTestEntry entry = CiFlakyTestEntry.builder()
                    .testKey(metric.getTestKey())
                    .className(metric.getClassName())
                    .methodName(metric.getMethodName())
                    .suite(metric.getSuite())
                    .classification(classification)
                    .failedInCurrentRun(failedNow)
                    .passedInCurrentRun(passedNow)
                    .recoveredThisRun(recovered)
                    .flakinessPercent(metric.getFlakinessPercent())
                    .totalRuns(metric.getTotalRuns())
                    .passCount(metric.getPassCount())
                    .failCount(metric.getFailCount() + metric.getBrokenCount())
                    .durationCv(duration.getCoefficientOfVariation())
                    .avgDurationMs(duration.getAvgDurationMs())
                    .minDurationMs(duration.getMinDurationMs())
                    .maxDurationMs(duration.getMaxDurationMs())
                    .currentOutcome(current != null ? current.getOutcome().name() : "NOT_RUN")
                    .build();

            flakyKeys.add(metric.getTestKey());
            flakyEntries.add(entry);
        }

        flakyEntries.sort(Comparator
                .comparing(CiFlakyTestEntry::isFailedInCurrentRun).reversed()
                .thenComparing(CiFlakyTestEntry::getFlakinessPercent, Comparator.reverseOrder()));

        List<CiFlakyTestEntry> failedOnly = new ArrayList<>();
        for (TestExecutionRecord current : currentRun) {
            if (!current.getOutcome().isFailure()) {
                continue;
            }
            if (flakyKeys.contains(current.getTestKey())) {
                continue;
            }
            failedOnly.add(CiFlakyTestEntry.builder()
                    .testKey(current.getTestKey())
                    .className(current.getClassName())
                    .methodName(current.getMethodName())
                    .suite(current.getSuite())
                    .classification(null)
                    .failedInCurrentRun(true)
                    .passedInCurrentRun(false)
                    .recoveredThisRun(false)
                    .flakinessPercent(0)
                    .totalRuns(1)
                    .passCount(0)
                    .failCount(1)
                    .durationCv(0)
                    .avgDurationMs(current.getDurationMs())
                    .minDurationMs(current.getDurationMs())
                    .maxDurationMs(current.getDurationMs())
                    .currentOutcome(current.getOutcome().name())
                    .build());
        }

        int currentPassed = (int) currentRun.stream().filter(r -> r.getOutcome() == TestOutcome.PASSED).count();
        int currentFailed = (int) currentRun.stream().filter(r -> r.getOutcome().isFailure()).count();

        int durationUnstableCount = (int) flakyEntries.stream()
                .filter(e -> e.getClassification() == FlakyClassification.DURATION_UNSTABLE
                        || e.getClassification() == FlakyClassification.INTERMITTENT_AND_DURATION)
                .count();
        int recoveredCount = (int) flakyEntries.stream().filter(CiFlakyTestEntry::isRecoveredThisRun).count();

        CiFlakyReport report = CiFlakyReport.builder()
                .analyzedAt(Instant.now())
                .runId(request.runId())
                .workflow(request.workflow())
                .currentRunTotal(currentRun.size())
                .currentRunPassed(currentPassed)
                .currentRunFailed(currentFailed)
                .flakyCount(flakyEntries.size())
                .failedOnlyCount(failedOnly.size())
                .durationUnstableCount(durationUnstableCount)
                .recoveredThisRunCount(recoveredCount)
                .historyRunCount(historyDoc.runs.size())
                .flakyTests(flakyEntries)
                .failedTests(failedOnly)
                .build();

        report = CiFlakyReport.builder()
                .analyzedAt(report.getAnalyzedAt())
                .runId(report.getRunId())
                .workflow(report.getWorkflow())
                .currentRunTotal(report.getCurrentRunTotal())
                .currentRunPassed(report.getCurrentRunPassed())
                .currentRunFailed(report.getCurrentRunFailed())
                .flakyCount(report.getFlakyCount())
                .failedOnlyCount(report.getFailedOnlyCount())
                .durationUnstableCount(report.getDurationUnstableCount())
                .recoveredThisRunCount(report.getRecoveredThisRunCount())
                .historyRunCount(report.getHistoryRunCount())
                .flakyTests(report.getFlakyTests())
                .failedTests(report.getFailedTests())
                .summaryLines(summaryWriter.buildSummaryLines(report))
                .build();

        historyStore.appendRun(historyDoc, request.runId(), request.workflow(), currentRun);
        historyStore.save(request.historyFile(), historyDoc);

        Path outputDir = request.outputDir();
        jsonWriter.write(report, outputDir.resolve("flaky-report.json"));
        htmlWriter.write(report, outputDir.resolve("flaky-report.html"));
        summaryWriter.writeGitHubStepSummary(report, request.summaryFile());

        log.info("Flaky analysis complete: {} flaky, {} failed-only (non-flaky), history runs={}",
                report.getFlakyCount(), report.getFailedOnlyCount(), report.getHistoryRunCount());
        return report;
    }

    private List<TestExecutionRecord> loadCurrentRun(Path allureDir) {
        return new AllureResultsLoader("ci-current", allureDir, objectMapper).load();
    }

    private List<TestExecutionRecord> tagCurrentRun(List<TestExecutionRecord> records, String runId) {
        return records.stream()
                .map(r -> TestExecutionRecord.builder()
                        .testKey(r.getTestKey())
                        .className(r.getClassName())
                        .methodName(r.getMethodName())
                        .suite(r.getSuite())
                        .outcome(r.getOutcome())
                        .message(r.getMessage())
                        .stackTrace(r.getStackTrace())
                        .source("current:" + runId)
                        .durationMs(r.getDurationMs())
                        .build())
                .toList();
    }

    private static FlakyClassification classify(boolean intermittent, boolean durationUnstable) {
        if (intermittent && durationUnstable) {
            return FlakyClassification.INTERMITTENT_AND_DURATION;
        }
        if (intermittent) {
            return FlakyClassification.INTERMITTENT_OUTCOME;
        }
        return FlakyClassification.DURATION_UNSTABLE;
    }

    public record CiFlakyAnalysisRequest(
            Path allureResultsDir,
            Path historyFile,
            Path outputDir,
            Path summaryFile,
            String runId,
            String workflow) {
    }

    public static void main(String[] args) throws Exception {
        Path allureDir = Path.of(env("ALLURE_RESULTS_DIR", "merged-allure-results"));
        Path historyFile = Path.of(env("FLAKY_HISTORY_FILE", "flaky-history/flaky-test-history.json"));
        Path outputDir = Path.of(env("FLAKY_OUTPUT_DIR", "flaky-report"));
        Path summaryFile = Path.of(env("GITHUB_STEP_SUMMARY", "/dev/null"));
        String runId = env("GITHUB_RUN_ID", "local");
        String workflow = env("GITHUB_WORKFLOW", "nightly-regression");

        new CiFlakyTestAnalyzer().analyze(new CiFlakyAnalysisRequest(
                allureDir, historyFile, outputDir, summaryFile, runId, workflow));
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
