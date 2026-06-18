package com.flowiq.agents.rootcause.loader;

import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
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
public class BackendLogCorrelator {

    private final Path logDirectory;

    public BackendLogCorrelator(RootCauseAgentConfig config) {
        this.logDirectory = resolvePath(config.backendLogDirectory());
    }

    public Map<String, List<String>> correlate(List<TestExecutionRecord> failures) {
        Map<String, List<String>> correlated = new LinkedHashMap<>();
        if (!Files.isDirectory(logDirectory)) {
            return correlated;
        }
        List<String> logLines = readLogLines();
        if (logLines.isEmpty()) {
            return correlated;
        }
        for (TestExecutionRecord failure : failures) {
            correlated.put(failure.getTestKey(), findMatches(failure, logLines));
        }
        return correlated;
    }

    private List<String> findMatches(TestExecutionRecord failure, List<String> logLines) {
        List<String> matches = new ArrayList<>();
        String method = failure.getMethodName() == null ? "" : failure.getMethodName().toLowerCase(Locale.ROOT);
        String simpleClass = simpleClassName(failure.getClassName()).toLowerCase(Locale.ROOT);
        String message = failure.getMessage() == null ? "" : failure.getMessage().toLowerCase(Locale.ROOT);

        for (String line : logLines) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (line.contains("ERROR") || line.contains("Exception") || line.contains("WARN")) {
                if ((!method.isBlank() && lower.contains(method))
                        || (!simpleClass.isBlank() && lower.contains(simpleClass))
                        || containsStatusHint(lower, message)) {
                    matches.add(line.trim());
                }
            }
        }
        if (matches.isEmpty()) {
            logLines.stream()
                    .filter(line -> line.contains("ERROR") || line.contains("Exception"))
                    .limit(3)
                    .map(String::trim)
                    .forEach(matches::add);
        }
        return matches.stream().limit(5).toList();
    }

    private static boolean containsStatusHint(String logLine, String message) {
        if (message.contains("500") && logLine.contains("500")) {
            return true;
        }
        if (message.contains("401") && logLine.contains("401")) {
            return true;
        }
        if (message.contains("403") && logLine.contains("403")) {
            return true;
        }
        return message.contains("internal server error") && logLine.contains("internal server error");
    }

    private List<String> readLogLines() {
        List<String> lines = new ArrayList<>();
        try (Stream<Path> files = Files.walk(logDirectory)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".log") || name.endsWith(".txt");
                    })
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(path -> appendLines(path, lines));
        } catch (IOException e) {
            log.warn("Failed to read backend logs from {}: {}", logDirectory, e.getMessage());
        }
        return lines;
    }

    private static void appendLines(Path path, List<String> lines) {
        try {
            lines.addAll(Files.readAllLines(path));
        } catch (IOException e) {
            log.debug("Skipping log file {}: {}", path, e.getMessage());
        }
    }

    private static String simpleClassName(String className) {
        if (className == null || className.isBlank()) {
            return "";
        }
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private static Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
