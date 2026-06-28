package com.flowiq.ci.flaky.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flowiq.agents.flaky.model.TestExecutionRecord;
import com.flowiq.agents.flaky.model.TestOutcome;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FlakyHistoryStore {

    public static final String SOURCE_BUSINESS = "business-tests";

    private final ObjectMapper objectMapper;
    private final int maxRuns;

    public FlakyHistoryStore() {
        this(30);
    }

    public FlakyHistoryStore(int maxRuns) {
        this.maxRuns = maxRuns;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public FlakyHistoryDocument load(Path historyFile) {
        if (historyFile == null || !Files.isRegularFile(historyFile)) {
            return FlakyHistoryDocument.empty(maxRuns);
        }
        try {
            FlakyHistoryDocument doc = objectMapper.readValue(historyFile.toFile(), FlakyHistoryDocument.class);
            if (doc.runs == null) {
                doc.runs = new ArrayList<>();
            }
            return doc;
        } catch (IOException e) {
            log.warn("Could not read flaky history from {}: {}", historyFile, e.getMessage());
            return FlakyHistoryDocument.empty(maxRuns);
        }
    }

    public void save(Path historyFile, FlakyHistoryDocument document) throws IOException {
        if (historyFile.getParent() != null) {
            Files.createDirectories(historyFile.getParent());
        }
        document.version = 1;
        document.maxRuns = maxRuns;
        trim(document);
        objectMapper.writeValue(historyFile.toFile(), document);
    }

    public List<TestExecutionRecord> toRecords(FlakyHistoryDocument document) {
        List<TestExecutionRecord> records = new ArrayList<>();
        for (FlakyRunSnapshot run : document.runs) {
            if (!SOURCE_BUSINESS.equals(run.source)) {
                continue;
            }
            for (ExecutionSnapshot exec : run.executions) {
                records.add(TestExecutionRecord.builder()
                        .testKey(exec.testKey)
                        .className(exec.className)
                        .methodName(exec.methodName)
                        .suite(exec.suite)
                        .outcome(TestOutcome.valueOf(exec.outcome))
                        .source("history:" + run.runId)
                        .durationMs(exec.durationMs)
                        .build());
            }
        }
        return records;
    }

    public void appendRun(FlakyHistoryDocument document, String runId, String workflow,
                          List<TestExecutionRecord> currentRunRecords) {
        FlakyRunSnapshot snapshot = new FlakyRunSnapshot();
        snapshot.runId = runId;
        snapshot.workflow = workflow;
        snapshot.timestamp = Instant.now().toString();
        snapshot.source = SOURCE_BUSINESS;
        snapshot.executions = currentRunRecords.stream()
                .map(r -> {
                    ExecutionSnapshot e = new ExecutionSnapshot();
                    e.testKey = r.getTestKey();
                    e.className = r.getClassName();
                    e.methodName = r.getMethodName();
                    e.suite = r.getSuite();
                    e.outcome = r.getOutcome().name();
                    e.durationMs = r.getDurationMs();
                    return e;
                })
                .toList();
        document.runs.add(snapshot);
        trim(document);
    }

    private void trim(FlakyHistoryDocument document) {
        while (document.runs.size() > maxRuns) {
            document.runs.remove(0);
        }
    }

    public static class FlakyHistoryDocument {
        public int version = 1;
        public int maxRuns = 30;
        public List<FlakyRunSnapshot> runs = new ArrayList<>();

        static FlakyHistoryDocument empty(int maxRuns) {
            FlakyHistoryDocument doc = new FlakyHistoryDocument();
            doc.maxRuns = maxRuns;
            doc.runs = new ArrayList<>();
            return doc;
        }
    }

    public static class FlakyRunSnapshot {
        public String runId;
        public String workflow;
        public String timestamp;
        public String source = SOURCE_BUSINESS;
        public List<ExecutionSnapshot> executions = List.of();
    }

    public static class ExecutionSnapshot {
        public String testKey;
        public String className;
        public String methodName;
        public String suite;
        public String outcome;
        public long durationMs;
    }
}
