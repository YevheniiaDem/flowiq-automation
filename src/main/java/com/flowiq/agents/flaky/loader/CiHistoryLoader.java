package com.flowiq.agents.flaky.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.flaky.config.FlakyTestAgentConfig;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
public class CiHistoryLoader {

    private final FlakyTestAgentConfig config;
    private final ObjectMapper objectMapper;

    public CiHistoryLoader(FlakyTestAgentConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public List<CiRunSummary> loadRunSummaries() {
        List<CiRunSummary> summaries = new ArrayList<>();
        summaries.addAll(loadLocalHistory());
        if (config.useGhCli()) {
            summaries.addAll(fetchFromGhCli());
        }
        return summaries;
    }

    public Map<String, Integer> failureCountsByTest() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CiRunSummary summary : loadRunSummaries()) {
            for (String failedTest : summary.failedTests()) {
                counts.merge(failedTest, 1, Integer::sum);
            }
        }
        return counts;
    }

    private List<CiRunSummary> loadLocalHistory() {
        List<CiRunSummary> summaries = new ArrayList<>();
        Path dir = resolvePath(config.ciHistoryDirectory());
        if (!Files.isDirectory(dir)) {
            return summaries;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> parseSummary(path).ifPresent(summaries::add));
        } catch (IOException e) {
            log.warn("Failed to read CI history from {}: {}", dir, e.getMessage());
        }
        log.info("Loaded {} CI run summaries from {}", summaries.size(), dir);
        return summaries;
    }

    private List<CiRunSummary> fetchFromGhCli() {
        List<CiRunSummary> summaries = new ArrayList<>();
        if (!isGhAvailable()) {
            log.debug("gh CLI not available — skipping live GitHub Actions fetch");
            return summaries;
        }
        String repo = config.githubRepository();
        if (repo == null || repo.isBlank()) {
            log.debug("agent.flaky.github.repository not set — skipping gh fetch");
            return summaries;
        }
        for (String workflow : config.githubWorkflows().split(",")) {
            String trimmed = workflow.trim();
            if (trimmed.isBlank()) continue;
            summaries.addAll(fetchWorkflowRuns(repo, trimmed));
        }
        return summaries;
    }

    private List<CiRunSummary> fetchWorkflowRuns(String repo, String workflow) {
        List<CiRunSummary> summaries = new ArrayList<>();
        try {
            Process process = new ProcessBuilder(
                    "gh", "run", "list",
                    "--repo", repo,
                    "--workflow", workflow,
                    "--limit", "30",
                    "--json", "databaseId,conclusion,createdAt,displayTitle,workflowName")
                    .redirectErrorStream(true)
                    .start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().reduce("", (a, b) -> a + b);
            }
            if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) {
                log.warn("gh run list failed for workflow {}", workflow);
                return summaries;
            }
            JsonNode runs = objectMapper.readTree(output);
            if (!runs.isArray()) {
                return summaries;
            }
            for (JsonNode run : runs) {
                summaries.add(new CiRunSummary(
                        run.path("workflowName").asText(workflow),
                        run.path("databaseId").asLong(),
                        run.path("conclusion").asText("unknown"),
                        run.path("createdAt").asText(""),
                        List.of()));
            }
            log.info("Fetched {} GitHub Actions runs for workflow {}", runs.size(), workflow);
        } catch (Exception e) {
            log.warn("Failed to fetch GitHub Actions history for {}: {}", workflow, e.getMessage());
        }
        return summaries;
    }

    private java.util.Optional<CiRunSummary> parseSummary(Path file) {
        try {
            JsonNode node = objectMapper.readTree(file.toFile());
            List<String> failedTests = new ArrayList<>();
            if (node.has("failedTests") && node.get("failedTests").isArray()) {
                node.get("failedTests").forEach(t -> failedTests.add(t.asText()));
            }
            return java.util.Optional.of(new CiRunSummary(
                    node.path("workflow").asText("local"),
                    node.path("runId").asLong(0),
                    node.path("conclusion").asText("unknown"),
                    node.path("createdAt").asText(""),
                    failedTests));
        } catch (IOException e) {
            log.warn("Skipping CI history file {}: {}", file, e.getMessage());
            return java.util.Optional.empty();
        }
    }

    private boolean isGhAvailable() {
        try {
            Process process = new ProcessBuilder("gh", "--version").start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Path resolvePath(String configured) {
        Path path = Paths.get(configured);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }

    public record CiRunSummary(String workflow, long runId, String conclusion, String createdAt,
                               List<String> failedTests) {
    }
}
