package com.flowiq.agents.flaky.loader;

import com.flowiq.agents.flaky.config.FlakyTestAgentConfig;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TestExecutionAggregator {

    private final FlakyTestAgentConfig config;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public TestExecutionAggregator(FlakyTestAgentConfig config,
                                   com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public List<TestExecutionRecord> loadAll() {
        List<TestExecutionRecord> records = new ArrayList<>();
        List<String> sources = new ArrayList<>();

        for (Path allureDir : resolveAllureDirectories()) {
            AllureResultsLoader loader = new AllureResultsLoader("allure", allureDir, objectMapper);
            records.addAll(loader.load());
            sources.add(loader.sourceName() + ":" + allureDir);
        }

        for (Path surefireDir : resolveSurefireDirectories()) {
            SurefireReportLoader loader = new SurefireReportLoader("surefire", surefireDir);
            records.addAll(loader.load());
            sources.add(loader.sourceName() + ":" + surefireDir);
        }

        records.addAll(enrichFromLogs(records));
        log.info("Aggregated {} execution records from {} source(s)", records.size(), sources.size());
        return records;
    }

    public String summarizeSources() {
        List<String> parts = new ArrayList<>();
        resolveAllureDirectories().forEach(p -> parts.add("Allure: " + p));
        resolveSurefireDirectories().forEach(p -> parts.add("Surefire: " + p));
        parts.add("CI history: " + resolvePath(config.ciHistoryDirectory()));
        parts.add("Logs: " + resolvePath(config.logDirectory()));
        return String.join("; ", parts);
    }

    private List<Path> resolveAllureDirectories() {
        List<Path> dirs = new ArrayList<>();
        if (config.allureResultsDirectories() != null) {
            Arrays.stream(config.allureResultsDirectories().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(this::resolvePath)
                    .forEach(dirs::add);
        }
        Path history = resolvePath(config.allureHistoryDirectory());
        if (Files.isDirectory(history)) {
            try (Stream<Path> runs = Files.list(history)) {
                runs.filter(Files::isDirectory).forEach(dirs::add);
            } catch (IOException e) {
                log.warn("Failed to list Allure history: {}", e.getMessage());
            }
        }
        return dirs.stream().distinct().collect(Collectors.toList());
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

    private List<TestExecutionRecord> enrichFromLogs(List<TestExecutionRecord> existing) {
        Path logDir = resolvePath(config.logDirectory());
        if (!Files.isDirectory(logDir)) {
            return List.of();
        }
        return List.of();
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
