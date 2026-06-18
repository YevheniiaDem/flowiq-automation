package com.flowiq.agents.rootcause.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.loader.AllureResultsLoader;
import com.flowiq.agents.flaky.loader.SurefireReportLoader;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.rootcause.config.RootCauseAgentConfig;
import com.flowiq.agents.rootcause.model.FailedTestContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FailureArtifactAggregator {

    private final RootCauseAgentConfig config;
    private final ObjectMapper objectMapper;
    private final BackendLogCorrelator logCorrelator;
    private final Path screenshotRoot;
    private final Path traceRoot;
    private final Path videoRoot;

    public FailureArtifactAggregator(RootCauseAgentConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.logCorrelator = new BackendLogCorrelator(config);
        this.screenshotRoot = resolvePath(config.screenshotDirectory());
        this.traceRoot = resolvePath(config.traceDirectory());
        this.videoRoot = resolvePath(config.videoDirectory());
    }

    public List<FailedTestContext> loadFailedTests() {
        List<TestExecutionRecord> records = loadExecutionRecords();
        Map<String, TestExecutionRecord> latestFailures = deduplicateLatestFailures(records);
        Map<String, List<String>> logCorrelations = logCorrelator.correlate(new ArrayList<>(latestFailures.values()));

        List<FailedTestContext> contexts = new ArrayList<>();
        for (TestExecutionRecord failure : latestFailures.values()) {
            contexts.add(buildContext(failure, logCorrelations.getOrDefault(failure.getTestKey(), List.of())));
        }
        contexts.sort(Comparator.comparing(c -> c.getExecution().getTestKey()));
        log.info("Prepared {} failed test context(s) for root cause analysis", contexts.size());
        return contexts;
    }

    public String summarizeSources() {
        List<String> parts = new ArrayList<>();
        resolveAllureDirectories().forEach(p -> parts.add("Allure: " + p));
        resolveSurefireDirectories().forEach(p -> parts.add("Surefire: " + p));
        parts.add("Screenshots: " + screenshotRoot);
        parts.add("Traces: " + traceRoot);
        parts.add("Videos: " + videoRoot);
        parts.add("Backend logs: " + resolvePath(config.backendLogDirectory()));
        return String.join("; ", parts);
    }

    private FailedTestContext buildContext(TestExecutionRecord failure, List<String> logLines) {
        String method = failure.getMethodName();
        String className = failure.getClassName();
        return FailedTestContext.builder()
                .execution(failure)
                .screenshots(findArtifacts(screenshotRoot, className, method,
                        List.of(".png", ".jpg", ".jpeg", ".webp")))
                .traces(findArtifacts(traceRoot, className, method, List.of(".zip", ".trace")))
                .videos(findArtifacts(videoRoot, className, method, List.of(".webm", ".mp4")))
                .backendLogLines(logLines)
                .build();
    }

    private List<Path> findArtifacts(Path root, String className, String method, List<String> extensions) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        String simpleClass = simpleClassName(className).toLowerCase(Locale.ROOT);
        String methodLower = method == null ? "" : method.toLowerCase(Locale.ROOT);
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                        boolean extMatch = extensions.stream().anyMatch(name::endsWith);
                        if (!extMatch) {
                            return false;
                        }
                        return name.contains(methodLower) || name.contains(simpleClass);
                    })
                    .limit(3)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private List<TestExecutionRecord> loadExecutionRecords() {
        List<TestExecutionRecord> records = new ArrayList<>();
        for (Path dir : resolveAllureDirectories()) {
            records.addAll(new AllureResultsLoader("allure", dir, objectMapper).load());
        }
        for (Path dir : resolveSurefireDirectories()) {
            records.addAll(new SurefireReportLoader("surefire", dir).load());
        }
        return records;
    }

    private static Map<String, TestExecutionRecord> deduplicateLatestFailures(List<TestExecutionRecord> records) {
        return records.stream()
                .filter(r -> r.getOutcome() != null && r.getOutcome().isFailure())
                .collect(Collectors.toMap(
                        TestExecutionRecord::getTestKey,
                        r -> r,
                        (left, right) -> right,
                        LinkedHashMap::new));
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

    private List<Path> resolveSurefireDirectories() {
        return resolvePath(config.surefireDirectory()).toString().isBlank()
                ? List.of()
                : List.of(resolvePath(config.surefireDirectory())).stream()
                .filter(Files::isDirectory)
                .toList();
    }

    private static String simpleClassName(String className) {
        if (className == null || className.isBlank()) {
            return "";
        }
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
