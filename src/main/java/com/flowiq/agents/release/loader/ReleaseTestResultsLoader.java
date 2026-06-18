package com.flowiq.agents.release.loader;

import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.loader.SurefireReportLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import com.flowiq.agents.gap.scanner.TestSuiteClassifier;
import com.flowiq.agents.model.TestSuiteType;
import com.flowiq.agents.release.config.ReleaseRiskAgentConfig;
import com.flowiq.agents.release.model.SuiteExecutionSummary;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ReleaseTestResultsLoader {

    private final ReleaseRiskAgentConfig config;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public ReleaseTestResultsLoader(ReleaseRiskAgentConfig config,
                                    com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public Map<TestSuiteType, SuiteExecutionSummary> loadSuiteSummaries() {
        List<TestExecutionRecord> records = loadAllRecords();
        return summarize(records, summarizeSources());
    }

    public List<TestExecutionRecord> loadAllRecords() {
        List<TestExecutionRecord> records = new ArrayList<>();
        for (Path dir : resolveSurefireDirectories()) {
            records.addAll(new SurefireReportLoader("surefire", dir).load());
        }
        for (Path dir : resolveAllureDirectories()) {
            records.addAll(new AllureResultsLoader("allure", dir, objectMapper).load());
        }
        log.info("Loaded {} execution record(s) for release assessment", records.size());
        return deduplicateLatest(records);
    }

    public Map<TestSuiteType, SuiteExecutionSummary> summarize(List<TestExecutionRecord> records,
                                                                String dataSource) {
        Map<TestSuiteType, SuiteExecutionSummary> summaries = new EnumMap<>(TestSuiteType.class);
        for (TestSuiteType type : List.of(TestSuiteType.REGRESSION, TestSuiteType.SMOKE, TestSuiteType.CONTRACT)) {
            summaries.put(type, buildSummary(type, records, dataSource));
        }
        return summaries;
    }

    private SuiteExecutionSummary buildSummary(TestSuiteType suiteType,
                                               List<TestExecutionRecord> records,
                                               String dataSource) {
        List<TestExecutionRecord> suiteRecords = records.stream()
                .filter(r -> matchesSuite(r, suiteType))
                .toList();

        int passed = 0;
        int failed = 0;
        int broken = 0;
        int skipped = 0;
        List<String> failures = new ArrayList<>();

        for (TestExecutionRecord record : suiteRecords) {
            switch (record.getOutcome()) {
                case PASSED -> passed++;
                case FAILED -> {
                    failed++;
                    failures.add(formatFailure(record));
                }
                case BROKEN -> {
                    broken++;
                    failures.add(formatFailure(record));
                }
                case SKIPPED -> skipped++;
            }
        }

        int decisive = passed + failed + broken;
        double passRate = decisive == 0 ? 100.0 : (passed * 100.0) / decisive;

        return SuiteExecutionSummary.builder()
                .suiteType(suiteType)
                .totalTests(suiteRecords.size())
                .passed(passed)
                .failed(failed)
                .broken(broken)
                .skipped(skipped)
                .passRate(passRate)
                .failureDetails(failures)
                .dataSource(dataSource)
                .build();
    }

    private static boolean matchesSuite(TestExecutionRecord record, TestSuiteType suiteType) {
        String simpleClass = simpleClassName(record.getClassName());
        Set<TestSuiteType> classified = TestSuiteClassifier.classify(simpleClass, record.getSource());
        if (!classified.isEmpty()) {
            return classified.contains(suiteType);
        }
        return suiteType.name().equalsIgnoreCase(record.getSuite());
    }

    private static String simpleClassName(String className) {
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private static String formatFailure(TestExecutionRecord record) {
        String message = record.getMessage() == null || record.getMessage().isBlank()
                ? "No message" : record.getMessage();
        return record.getTestKey() + " — " + truncate(message, 120);
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private List<TestExecutionRecord> deduplicateLatest(List<TestExecutionRecord> records) {
        Map<String, TestExecutionRecord> latest = new LinkedHashMap<>();
        for (TestExecutionRecord record : records) {
            latest.put(record.getTestKey(), record);
        }
        return List.copyOf(latest.values());
    }

    public String summarizeSources() {
        List<String> parts = new ArrayList<>();
        resolveSurefireDirectories().forEach(p -> parts.add("Surefire: " + p));
        resolveAllureDirectories().forEach(p -> parts.add("Allure: " + p));
        return String.join("; ", parts);
    }

    private List<Path> resolveSurefireDirectories() {
        List<Path> dirs = new ArrayList<>();
        dirs.add(resolvePath(config.surefireDirectory()));
        Path history = resolvePath(config.surefireHistoryDirectory());
        if (Files.isDirectory(history)) {
            try (Stream<Path> runs = Files.list(history)) {
                runs.filter(Files::isDirectory).forEach(dirs::add);
            } catch (IOException e) {
                log.warn("Failed to list Surefire history: {}", e.getMessage());
            }
        }
        return dirs.stream().filter(Files::isDirectory).distinct().collect(Collectors.toList());
    }

    private List<Path> resolveAllureDirectories() {
        List<Path> dirs = new ArrayList<>();
        if (config.allureResultsDirectories() != null) {
            Arrays.stream(config.allureResultsDirectories().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(this::resolvePath)
                    .filter(Files::isDirectory)
                    .forEach(dirs::add);
        }
        return dirs;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
