package com.flowiq.agents.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowiq.agents.config.AgentConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
public class OpenApiSnapshotStore {

    private final AgentConfig agentConfig;
    private final ObjectMapper objectMapper;
    private final Path snapshotPath;

    public OpenApiSnapshotStore(AgentConfig agentConfig, ObjectMapper objectMapper) {
        this.agentConfig = agentConfig;
        this.objectMapper = objectMapper;
        this.snapshotPath = resolveSnapshotPath();
    }

    public Optional<JsonNode> loadPreviousSnapshot() {
        if (!Files.exists(snapshotPath)) {
            log.warn("No previous OpenAPI snapshot found at {}", snapshotPath.toAbsolutePath());
            return Optional.empty();
        }
        try {
            log.info("Loading previous OpenAPI snapshot from {}", snapshotPath.toAbsolutePath());
            return Optional.of(objectMapper.readTree(snapshotPath.toFile()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load snapshot from " + snapshotPath, e);
        }
    }

    public void saveSnapshot(JsonNode spec) {
        try {
            Files.createDirectories(snapshotPath.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(snapshotPath.toFile(), spec);

            Path timestamped = snapshotPath.getParent().resolve(
                    "openapi-snapshot-" + DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                            .replace(":", "-") + ".json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(timestamped.toFile(), spec);
            log.info("Saved OpenAPI snapshot to {} and {}", snapshotPath, timestamped);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save snapshot to " + snapshotPath, e);
        }
    }

    public Path getSnapshotPath() {
        return snapshotPath;
    }

    private Path resolveSnapshotPath() {
        Path base = Paths.get(agentConfig.snapshotDirectory());
        if (!base.isAbsolute()) {
            base = Paths.get(System.getProperty("user.dir")).resolve(base);
        }
        return base.resolve(agentConfig.snapshotFilename());
    }
}
