package com.flowiq.agents.selfhealing.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.model.TestOutcome;
import com.flowiq.agents.selfhealing.config.SelfHealingAgentConfig;
import com.flowiq.agents.selfhealing.model.LocatorFailureContext;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
public class UiFailureArtifactLoader {

    private final SelfHealingAgentConfig config;
    private final ObjectMapper objectMapper;
    private final DomSnapshotCollector domSnapshotCollector;
    private final Path domRoot;
    private final Path screenshotRoot;

    public UiFailureArtifactLoader(SelfHealingAgentConfig config,
                                   ObjectMapper objectMapper,
                                   DomSnapshotCollector domSnapshotCollector) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.domSnapshotCollector = domSnapshotCollector;
        this.domRoot = resolvePath(config.domSnapshotDirectory());
        this.screenshotRoot = resolvePath(config.screenshotDirectory());
    }

    public List<LocatorFailureContext> loadFailures() {
        List<LocatorFailureContext> contexts = new ArrayList<>();
        for (Path allureDir : resolveAllureDirectories()) {
            contexts.addAll(loadFromAllure(allureDir));
        }
        log.info("Loaded {} UI locator failure context(s)", contexts.size());
        return contexts;
    }

    private List<LocatorFailureContext> loadFromAllure(Path directory) {
        List<LocatorFailureContext> contexts = new ArrayList<>();
        if (!Files.isDirectory(directory)) {
            return contexts;
        }
        try (Stream<Path> files = Files.walk(directory)) {
            files.filter(p -> p.getFileName().toString().endsWith("-result.json"))
                    .forEach(path -> parseAllureFailure(path).ifPresent(contexts::add));
        } catch (IOException e) {
            log.warn("Failed to walk Allure directory {}: {}", directory, e.getMessage());
        }
        return contexts;
    }

    private java.util.Optional<LocatorFailureContext> parseAllureFailure(Path file) {
        try {
            JsonNode node = objectMapper.readTree(file.toFile());
            String status = text(node, "status");
            if (!TestOutcome.FAILED.name().equalsIgnoreCase(status)
                    && !TestOutcome.BROKEN.name().equalsIgnoreCase(status)) {
                return java.util.Optional.empty();
            }
            String fullName = text(node, "fullName");
            String className = extractClassName(fullName);
            if (!isUiTest(className)) {
                return java.util.Optional.empty();
            }
            String methodName = text(node, "name");
            if (methodName.isBlank()) {
                methodName = extractMethodName(fullName);
            }
            String testKey = !fullName.isBlank() ? fullName : className + "#" + methodName;
            JsonNode statusDetails = node.path("statusDetails");
            String message = text(statusDetails, "message");
            String trace = text(statusDetails, "trace");

            Path domSnapshot = resolveDomSnapshot(className, methodName);
            Path screenshot = resolveScreenshot(className, methodName);

            return java.util.Optional.of(LocatorFailureContext.builder()
                    .testKey(testKey)
                    .testName(methodName)
                    .className(className)
                    .methodName(methodName)
                    .failureMessage(message)
                    .stackTrace(trace)
                    .screenshotPath(screenshot)
                    .domSnapshotPath(domSnapshot)
                    .domElements(domSnapshotCollector.collect(domSnapshot))
                    .build());
        } catch (IOException e) {
            log.warn("Skipping Allure file {}: {}", file, e.getMessage());
            return java.util.Optional.empty();
        }
    }

    private Path resolveDomSnapshot(String className, String methodName) {
        String simpleClass = simpleClassName(className);
        List<String> candidates = List.of(
                simpleClass + "_" + methodName + ".html",
                simpleClass + "-" + methodName + ".html",
                methodName + ".html",
                simpleClass.toLowerCase(Locale.ROOT) + ".html"
        );
        for (String name : candidates) {
            Path path = domRoot.resolve(name);
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }

    private Path resolveScreenshot(String className, String methodName) {
        String simpleClass = simpleClassName(className);
        if (!Files.isDirectory(screenshotRoot)) {
            return null;
        }
        try (Stream<Path> files = Files.list(screenshotRoot)) {
            return files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.contains(methodName.toLowerCase(Locale.ROOT))
                                || name.contains(simpleClass.toLowerCase(Locale.ROOT));
                    })
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isUiTest(String className) {
        String lower = className.toLowerCase(Locale.ROOT);
        return lower.contains(".ui.") || lower.contains("smoketest") || lower.contains("uismoke");
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

    public String summarizeSources() {
        List<String> parts = new ArrayList<>();
        resolveAllureDirectories().forEach(p -> parts.add("Allure: " + p));
        parts.add("DOM: " + domRoot);
        parts.add("Screenshots: " + screenshotRoot);
        return String.join("; ", parts);
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return "";
        }
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private static String extractClassName(String fullName) {
        if (fullName == null || !fullName.contains(".")) {
            return fullName == null ? "" : fullName;
        }
        int dot = fullName.lastIndexOf('.');
        return fullName.substring(0, dot);
    }

    private static String simpleClassName(String className) {
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private static String extractMethodName(String fullName) {
        int dot = fullName.lastIndexOf('.');
        return dot >= 0 ? fullName.substring(dot + 1) : fullName;
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
