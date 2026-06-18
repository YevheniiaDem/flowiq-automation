package com.flowiq.agents.flaky.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class AllureResultsLoader implements TestExecutionLoader {

    private final String sourceName;
    private final Path directory;
    private final ObjectMapper objectMapper;

    public AllureResultsLoader(String sourceName, Path directory, ObjectMapper objectMapper) {
        this.sourceName = sourceName;
        this.directory = directory;
        this.objectMapper = objectMapper;
    }

    @Override
    public String sourceName() {
        return sourceName;
    }

    @Override
    public List<TestExecutionRecord> load() {
        List<TestExecutionRecord> records = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            log.debug("Allure directory not found: {}", directory);
            return records;
        }
        try (Stream<Path> files = Files.walk(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith("-result.json"))
                    .forEach(path -> parseFile(path).ifPresent(records::add));
        } catch (IOException e) {
            log.warn("Failed to read Allure results from {}: {}", directory, e.getMessage());
        }
        log.info("Loaded {} Allure records from {}", records.size(), directory);
        return records;
    }

    private java.util.Optional<TestExecutionRecord> parseFile(Path file) {
        try {
            JsonNode node = objectMapper.readTree(file.toFile());
            String fullName = textOrEmpty(node, "fullName");
            String name = textOrEmpty(node, "name");
            String historyId = textOrEmpty(node, "historyId");
            String testKey = !fullName.isBlank() ? fullName
                    : (!historyId.isBlank() ? historyId : name);

            String className = extractClassName(fullName);
            String methodName = !name.isBlank() ? name : extractMethodName(fullName);

            JsonNode statusDetails = node.path("statusDetails");
            String message = textOrEmpty(statusDetails, "message");
            String trace = textOrEmpty(statusDetails, "trace");

            long duration = node.path("stop").asLong(0) - node.path("start").asLong(0);

            return java.util.Optional.of(TestExecutionRecord.builder()
                    .testKey(testKey)
                    .className(className)
                    .methodName(methodName)
                    .suite(inferSuite(className))
                    .outcome(TestOutcome.fromAllureStatus(textOrEmpty(node, "status")))
                    .message(message)
                    .stackTrace(trace)
                    .source(sourceName + ":" + directory.getFileName())
                    .durationMs(Math.max(duration, 0))
                    .build());
        } catch (IOException e) {
            log.warn("Skipping invalid Allure result {}: {}", file, e.getMessage());
            return java.util.Optional.empty();
        }
    }

    static String extractClassName(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return "unknown";
        }
        int hash = fullName.lastIndexOf('.');
        int methodStart = fullName.indexOf('.', fullName.lastIndexOf("com."));
        if (methodStart > 0 && methodStart < hash) {
            return fullName.substring(0, hash);
        }
        return fullName.contains("#")
                ? fullName.substring(0, fullName.indexOf('#'))
                : fullName.substring(0, hash);
    }

    static String extractMethodName(String fullName) {
        if (fullName == null) {
            return "unknown";
        }
        if (fullName.contains("#")) {
            return fullName.substring(fullName.indexOf('#') + 1);
        }
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    private static String inferSuite(String className) {
        if (className.contains("ContractTest")) return "contract";
        if (className.contains("Smoke")) return "smoke";
        if (className.contains("Regression")) return "regression";
        if (className.contains("Ui") || className.contains("E2E")) return "ui";
        if (className.contains("Integration")) return "integration";
        return "other";
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }
}
